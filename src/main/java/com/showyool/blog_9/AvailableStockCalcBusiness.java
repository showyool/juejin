package com.showyool.blog_9;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Order(1)
@Slf4j
public class AvailableStockCalcBusiness extends StockCalcBusiness {

    @Override
    public void stockCalc(Stock stock) {
        StockCalcManager.threadPool.submit(() -> {
            log.info("计算可用库存");
            if (StockType.checkAvailableStock(stock.getBit4Stock())) {
                // 这里就省略可用库存的获取过程
                stock.setAvailableStock(10L);
            } else {
                stock.setAvailableStock(0L);
            }
            try {
                Thread.sleep(1000 * new Random().nextInt(10));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stock.getCountDownLatch().countDown();
        });

    }

}
