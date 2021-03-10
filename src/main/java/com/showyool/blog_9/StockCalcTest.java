package com.showyool.blog_9;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StockCalcTest {

    @Autowired
    StockCalcManager stockCalcManager;

    @Test
    public void test(){
        Stock stock = new Stock();
        stockCalcManager.stockCalc(stock);
        System.out.println(stock.getTotalShowStock());

        stock.setBit4Stock(1);
        stockCalcManager.stockCalc(stock);
        System.out.println(stock.getTotalShowStock());

        stock.setBit4Stock(2);
        stockCalcManager.stockCalc(stock);
        System.out.println(stock.getTotalShowStock());

        stock.setBit4Stock(4);
        stockCalcManager.stockCalc(stock);
        System.out.println(stock.getTotalShowStock());

        stock.setBit4Stock(8);
        stockCalcManager.stockCalc(stock);
        System.out.println(stock.getTotalShowStock());
    }

}
