package com.showyool.blog_9;

import java.util.concurrent.CountDownLatch;

public class Stock {

    private CountDownLatch countDownLatch;
    /**
     * itemId是款ID
     * skuId是skuId
     * 两个ID确定最细粒度的商品
     */
    private Long itemId;
    private Long skuId;

    /**
     * 库存组合开关
     */
    private int bit4Stock = 0;

    private Long availableStock;
    private Long purchaseStock;
    private Long returnStock;
    private Long adjustStock;

    /**
     * 最终展示的库存
     */
    private Long totalShowStock;

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public int getBit4Stock() {
        return bit4Stock;
    }

    public void setBit4Stock(int bit4Stock) {
        this.bit4Stock = bit4Stock;
    }

    public Long getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Long availableStock) {
        this.availableStock = availableStock;
    }

    public Long getPurchaseStock() {
        return purchaseStock;
    }

    public void setPurchaseStock(Long purchaseStock) {
        this.purchaseStock = purchaseStock;
    }

    public Long getReturnStock() {
        return returnStock;
    }

    public void setReturnStock(Long returnStock) {
        this.returnStock = returnStock;
    }

    public Long getAdjustStock() {
        return adjustStock;
    }

    public void setAdjustStock(Long adjustStock) {
        this.adjustStock = adjustStock;
    }

    public Long getTotalShowStock() {
        return this.availableStock + this.purchaseStock + this.returnStock + this.adjustStock;
    }

    public void setTotalShowStock(Long totalShowStock) {
        this.totalShowStock = totalShowStock;
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

}
