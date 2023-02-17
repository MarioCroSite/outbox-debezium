package com.mario.inventoryservice.util;

public class MessageStatus {

    private MessageStatus() {

    }

    public static final String PRODUCT = "PRODUCT";

    public static final String RESERVE_CUSTOMER_BALANCE_SUCCESSFULLY =
            "RESERVE_CUSTOMER_BALANCE_SUCCESSFULLY";

    public static final String RESERVE_PRODUCT_STOCK_FAILED = "RESERVE_PRODUCT_STOCK_FAILED";

    public static final String RESERVE_PRODUCT_STOCK_SUCCESSFULLY =
            "RESERVE_PRODUCT_STOCK_SUCCESSFULLY";

    public static final String EVENT_TYPE = "eventType";
}
