package com.showyool.blog_9;

public class StockType {

    /**
     * 可用库存
     */
    private static final int AVAILABLE_STOCK         = 0b00000000000001;

    /**
     * 采购在途库存
     */
    private static final int PURCHASE_STOCK          = 0b00000000000010;

    /**
     * 销退在途库存
     */
    private static final int RETURN_STOCK            = 0b00000000000100;

    /**
     * 调整库存
     */
    private static final int ADJUST_STOCK            = 0b00000000001000;

    public static boolean checkAvailableStock(int mod) {
        return (mod & AVAILABLE_STOCK) != 0;
    }

    public static boolean checkPurchaseStock(int mod) {
        return (mod & PURCHASE_STOCK) != 0;
    }

    public static boolean checkReturnStock(int mod) {
        return (mod & RETURN_STOCK) != 0;
    }

    public static boolean checkAdjustStock(int mod) {
        return (mod & ADJUST_STOCK) != 0;
    }
}
