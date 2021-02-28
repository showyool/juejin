package com.showyool.blog_8;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@RestController
@Slf4j
public class InsertDataController {

    private SnowflakeIdWorker idWorker = new SnowflakeIdWorker(0, 0);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Autowired
    private TransactionTemplate transactionTemplate;

    private ExecutorService threadPool = new ThreadPoolExecutor(10, 10,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(10000));

    @RequestMapping(value = "/insertData", method = RequestMethod.GET)
    public Object insertData() {
        final String sql0 = Constants.insertMap.get(0);
        final String sql1 = Constants.insertMap.get(1);
        List<Object[]> params0 = Lists.newArrayListWithExpectedSize(1000);
        List<Object[]> params1 = Lists.newArrayListWithExpectedSize(1000);
        int index = 1;
        for (int i = 0; i < 100000; i++) {
            long id = idWorker.nextId();
            String userId = chooseUser();
            int userIdInt = Integer.parseInt(userId);
            String item = RandomStringUtils.random(2, true, false);
            Object[] param = new Object[]{id, userIdInt, item, 1};
            if ((userIdInt & 1) == 0) {
                params0.add(param);
            } else {
                params1.add(param);
            }
            if (i % 2000 == 0) {
                jdbcTemplate.batchUpdate(sql0, params0);
                jdbcTemplate.batchUpdate(sql1, params1);
                params0.clear();
                params1.clear();
                log.info("第" + index++ + "次数据插入");
            }
        }
        return "SUCCESS";
    }

    /**
     * 迁移工作，当做后台任务进行操作
     */
    @RequestMapping(value = "/migrationWork", method = RequestMethod.GET)
    public Object migrationWork() {
        String sql0 = "select * from order_0 where user_id = ? and id > ? order by id limit ?";
        String sql1 = "select * from order_1 where user_id = ? and id > ? order by id limit ?";
        for (int i = 0; i < 1000; i++) {

            log.info("userId: [" + i + "]迁移工作开始");
            long start = 0L;
            int index1 = i % 2;
            int index2 = i % 4;
            if (index1 == index2) {
                continue;
            }
            String selectSql = index1 == 0 ? sql0 : sql1;
            String insertSql = Constants.insertMap.get(index2);
            while (true) {
                List<Order> orders = this.jdbcTemplate.query(selectSql, new Object[]{i, start, 500}, new BeanPropertyRowMapper<>(Order.class));
                if (CollectionUtils.isEmpty(orders)) {
                    // 说明没有需要迁移的数据
                    break;
                }
                for (Order order : orders) {
                    try {
                        this.jdbcTemplate.update(insertSql, new Object[]{order.getId(), order.getUser_id(), order.getId(), order.getCount()});
                    } catch (Exception e) {
                        // 例如MySQLIntegrityConstraintViolationException主键冲突问题，则忽略
                        e.printStackTrace();
                    }
                }
                start = orders.get(orders.size() - 1).getId();
            }
            ConcurrentLinkedQueue<Tuple2<String, Object[]>> queue = null;
            RLock rLock = redissonClient.getLock(Constants.D_KEY + i);
            try {
                rLock.lock();
                queue = Constants.appendSQLs.get(i);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                rLock.unlock();
            }
            while (queue != null && !queue.isEmpty()) {
                Tuple2<String, Object[]> tuple2 = queue.poll();
                this.jdbcTemplate.update(tuple2.getT1(), tuple2.getT2());
            }
            RLock dUserLock = redissonClient.getLock(Constants.D_USER_KEY + i);
            try {
                dUserLock.lock();
                redissonClient.getBucket(Constants.USER_KEY + i).delete();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                dUserLock.unlock();
            }
            log.info("userId: [" + i + "]迁移工作结束");
        }
        // 开始检查工作
        doCheckWork();
        return "SUCCESS";
    }

    /**
     * 双写
     * 这里其实是为了模拟正常环境中用户流量
     * 存在增删改查
     */
    @RequestMapping(value = "/doubleWrite", method = RequestMethod.GET)
    public Object doubleWrite() {
        // 模拟1000个请求
        // 对库表会产生影响只有新增、删除、更新操作，所以这里的查询操作就不进行处理
        for (int i = 0; i < 2000; i++) {
            threadPool.submit(new EventWorker(Integer.parseInt(chooseUser()), chooseEvent(), jdbcTemplate, redissonClient, transactionTemplate));
        }
        return "SUCCESS";
    }

    /**
     * 在Redis当中初始化用户数据
     */
    @RequestMapping(value = "/initRedisUser", method = RequestMethod.GET)
    public Object initRedisUser() {
        for (int i = 0; i < 1000; i++) {
            RBucket<String> redisUser = redissonClient.getBucket(Constants.USER_KEY + i);
            redisUser.set("0");
        }
        return "SUCCESS";
    }

    @RequestMapping(value = "/checkWork", method = RequestMethod.GET)
    public Object checkWork() {
        doCheckWork();
        return "SUCCESS";
    }

    /**
     * 检查工作
     * 主要是为了检查两阶段的结果
     * 1. 迁移工作造成的数据差异（迁移工作应该要保证这一部分不要出错）
     * 2. 迁移工作之后正常流量的双写造成的数据差异
     * <p>
     * 此外，检查工作不是一次性任务，而是需要定时地进行查询
     * 这里为了简便，以查询每个用户下有多少订单和总共涉及到的商品数量当做检查
     * 每一个场景需要具体深入判断，这个视观众具体业务场景判断
     */
    private void doCheckWork() {
        Map<Integer, String> map1 = Maps.newHashMap();
        map1.put(0, "select count(*) from order_0 where user_id = ?");
        map1.put(1, "select count(*) from order_1 where user_id = ?");
        map1.put(2, "select count(*) from order_2 where user_id = ?");
        map1.put(3, "select count(*) from order_3 where user_id = ?");
        Map<Integer, String> map2 = Maps.newHashMap();
        map2.put(0, "select sum(count) from order_0 where user_id = ?");
        map2.put(1, "select sum(count) from order_1 where user_id = ?");
        map2.put(2, "select sum(count) from order_2 where user_id = ?");
        map2.put(3, "select sum(count) from order_3 where user_id = ?");
        // 由于测试数据是模拟0-1000这些用户的，所以这里就当做所有用户进行检查工作
        int pass = 0;
        int fail = 0;
        for (int i = 0; i < 1000; i++) {
            int from = i % 2;
            int to = i % 4;
            if (from == to) {
                pass++;
                continue;
            }
            Integer fromCount = jdbcTemplate.queryForObject(map1.get(from), new Object[]{i}, Integer.class);
            Integer toCount = jdbcTemplate.queryForObject(map1.get(to), new Object[]{i}, Integer.class);
            Integer fromSum = jdbcTemplate.queryForObject(map2.get(from), new Object[]{i}, Integer.class);
            Integer toSum = jdbcTemplate.queryForObject(map2.get(to), new Object[]{i}, Integer.class);
            if (fromCount.equals(toCount) && fromSum.equals(toSum)) {
                log.info("用户ID:" + i + "检查通过, 原表数量: " + fromCount + ", 新表数量: " + toCount + ", 原表商品总数: " + fromSum + ", 新表商品总数: " + toSum);
                pass++;
            } else {
                log.error("用户ID:" + i + "检查不通过，原表数量: " + fromCount + ", 新表数量: " + toCount + ", 原表商品总数: " + fromSum + ", 新表商品总数: " + toSum);
                fail++;
            }
        }
        System.out.println("检查通过: " + pass + "，检查失败: " + fail);
    }

    /**
     * 用户编号0-999，这样随机抽取一个
     */
    private static String chooseUser() {
        return RandomStringUtils.random(3, false, true);
    }

    /**
     * 增删改查事件，随机抽取一个
     */
    private static String chooseEvent() {
        return RandomStringUtils.random(1, new char[]{'i', 'd', 'u', 's'});
    }

}
