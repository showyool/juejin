package com.showyool.blog_9;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Slf4j
@Order(2)
public class PurchaseStockCalcBusiness extends StockCalcBusiness {

    @Override
    public void stockCalc(Stock stock) {
        StockCalcManager.threadPool.submit(() -> {
            log.info("计算采购在途库存");
            if (StockType.checkPurchaseStock(stock.getBit4Stock())) {
                // 这里就省略采购在途库存的获取过程
                stock.setPurchaseStock(40L);
            } else {
                stock.setPurchaseStock(0L);
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
