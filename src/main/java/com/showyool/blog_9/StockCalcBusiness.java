package com.showyool.blog_9;

public abstract class StockCalcBusiness {

    private StockCalcBusiness next;

    public final void handle(Stock stock) {
        // 先处理当前环境下的库存
        this.stockCalc(stock);
        // 再处理下一个责任链当中的库存
        if (this.next != null) {
            this.next.handle(stock);
        }
    }

    public void setNext(StockCalcBusiness stockCalcBusiness) {
        this.next = stockCalcBusiness;
    }

    public abstract void stockCalc(Stock stock);

}
