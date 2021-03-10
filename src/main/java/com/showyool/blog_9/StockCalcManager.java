package com.showyool.blog_9;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.*;

@Component
@Slf4j
public class StockCalcManager {

    @Autowired
    List<StockCalcBusiness> stockCalcBusinessList;

    public static ExecutorService threadPool = new ThreadPoolExecutor(10, 10,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(10000));

    @PostConstruct
    public void build() {
        if (CollectionUtils.isEmpty(stockCalcBusinessList)) {
            stockCalcBusinessList = Lists.newArrayList(new StockCalcBusiness() {
                @Override
                public void stockCalc(Stock stock) {
                    log.info("nothing to deal");
                }
            });
        } else {
            for (int i = 0; i < stockCalcBusinessList.size() - 1; i++) {
                stockCalcBusinessList.get(i).setNext(stockCalcBusinessList.get(i + 1));
            }
        }
    }

    public void stockCalc(Stock stock) {
        stock.setCountDownLatch(new CountDownLatch(stockCalcBusinessList.size()));
        this.stockCalcBusinessList.get(0).handle(stock);
        try {
            stock.getCountDownLatch().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("计算结束");
    }

}
