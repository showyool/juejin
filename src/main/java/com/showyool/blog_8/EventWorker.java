package com.showyool.blog_8;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 模拟真实用户流量，并对部分操作进行双写
 *
 * 这里涉及到的事务，由于本地模拟使用了Transactional
 * 该事务的作用域仅在单库情况下，在跨库的情况下，就不得不使用分布式事务解决方案
 * 例如阿里的seats
 *
 * 由于当前公司并没有支持seats，且当初水平扩容并未涉及到跨库，这里暂不考虑
 * 当然也可以使用BASE方案，追求最终一致性的话，那么也可以乐观双写，但是需要通过业务回调，依赖业务校准
 */
@Slf4j
public class EventWorker implements Runnable {

    private SnowflakeIdWorker idWorker = new SnowflakeIdWorker(0, 0);

    private int userId;
    private String eventType;
    private JdbcTemplate jdbcTemplate;
    private RedissonClient redissonClient;
    private TransactionTemplate transactionTemplate;
    
    public EventWorker(int userId, String eventType, JdbcTemplate jdbcTemplate, RedissonClient redissonClient, TransactionTemplate transactionTemplate) {
        this.userId = userId;
        this.eventType = eventType;
        this.jdbcTemplate = jdbcTemplate;
        this.redissonClient = redissonClient;
        this.transactionTemplate = transactionTemplate;
        log.info("userId: " + userId + ", eventType: " + eventType);
    }
    
    @Override
    public void run() {
        transactionTemplate.execute(transactionStatus -> {
            try {
                doWork();
                return true;
            } catch (Exception e) {
                transactionStatus.setRollbackOnly();
                return false;
            }
        });

    }
    
    public void doWork() {
        switch (eventType) {
            case "i": {
                // 新增
                doInsert();
                break;
            }
            case "d": {
                // 删除
                doDelete();
                break;
            }
            case "u": {
                // 更新
                doUpdate();
                break;
            }
            case "s":
                // 查询
                doSelect();
                break;
            default:
                break;
        }
    }

    /**
     * 由于查询不会对系统产生影响，所以这里可以不处理
     */
    private void doSelect() {

    }

    /**
     * 对于更新操作，需要考虑一个特殊的情况
     * 由于数据迁移的时候是先查询再插入的，所以当数据查询出来的时候，这个时候有可能新的数据已经更新
     * 当新表的记录还没有写入，双写更新的时候，新表的数据无法更新，相反，插入的还是旧数据
     */
    public void doUpdate() {
        log.info("开始更新...");
        int index1 = userId % 2;
        int index2 = userId % 4;
        if (index1 == index2) {
            return;
        }
        String selectSQL = "SELECT id FROM " + Constants.tableMap.get(index1) + " WHERE user_id = ? ORDER BY id DESC LIMIT 1";
        Long orderId = this.jdbcTemplate.queryForObject(selectSQL, new Object[] {userId}, Long.class);
        Integer updateCount = Integer.parseInt(RandomStringUtils.random(1, false, true));
        Object[] param = new Object[] {updateCount, orderId};
        // 这个是对旧表的更新
        this.jdbcTemplate.update(Constants.updateMap.get(index1), param);
        // 这个是对新表的更新 需要判断影响的行数

        RLock rLock = redissonClient.getLock(Constants.D_KEY);
        ConcurrentLinkedQueue<Tuple2<String, Object[]>> queue = null;
        try {
            rLock.lock();
            queue = Constants.appendSQLs.get(userId);
            if (queue == null) {
                Constants.appendSQLs.put(userId, new ConcurrentLinkedQueue<>());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        if (queue == null || queue.isEmpty()) {
            int effectRow = this.jdbcTemplate.update(Constants.updateMap.get(index2), param);
            if (effectRow == 0) {
                // 说明此刻数据还没有更新，需要加入到消费队列中
                log.info("userId: [" + userId + "]需要加入到队列的数据: " + JSON.toJSONString(param));
                if (queue == null) {
                    Constants.appendSQLs.put(userId, new ConcurrentLinkedQueue<>());
                }
                Constants.appendSQLs.get(userId).add(new Tuple2<>(Constants.updateMap.get(index2), param));
            }
        } else {
            log.info("userId: [" + userId + "]必须加入到队列的数据: " + JSON.toJSONString(param));
            queue.add(new Tuple2<>(Constants.updateMap.get(index2), param));
        }
    }

    /**
     * 对于删除场景比较简单，直接删除即可
     * 不会有其他影响
     */
    public void doDelete() {
        int index1 = userId % 2;
        int index2 = userId % 4;
        // 这里就取出最后一条
        String selectSQL = "SELECT id FROM order_" + Constants.tableMap.get(index1) + " WHERE user_id = ? ORDER BY id DESC LIMIT 1";
        Long orderId = this.jdbcTemplate.queryForObject(selectSQL, new Object[] {userId}, Long.class);
        Object[] param = new Object[] {orderId};
        this.jdbcTemplate.update(Constants.deleteMap.get(index1), param);
        this.jdbcTemplate.update(Constants.deleteMap.get(index2), param);
    }

    /**
     * 对于插入场景比较简单，直接插入即可
     * 因为主键不会重复，所以即便迁移程序还没有将新插入的记录进行迁移
     * 在迁移的时候发现新表已经有记录了，所以产生主键冲突，这里需要进行异常捕获
     * 这也导致了在迁移工作中不能批量插入，只能逐条插入
     */
    public void doInsert() {
        int index1 = userId % 2;
        int index2 = userId % 4;
        String sql1 = Constants.insertMap.get(index1);
        String sql2 = Constants.insertMap.get(index2);
        Object[] param = new Object[] {idWorker.nextId(), userId, RandomStringUtils.random(2, true, false), RandomStringUtils.random(1, false, true)};
        this.jdbcTemplate.update(sql1, param);
        this.jdbcTemplate.update(sql2, param);
    }

}
