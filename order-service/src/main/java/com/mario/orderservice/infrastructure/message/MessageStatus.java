package com.mario.orderservice.infrastructure.message;

public class MessageStatus {

    private MessageStatus() {
    }

    public static final String ORDER = "ORDER";

    public static final String ORDER_CREATED = "ORDER_CREATED";

    public static final String RESERVE_CUSTOMER_BALANCE_SUCCESSFULLY =
            "RESERVE_CUSTOMER_BALANCE_SUCCESSFULLY";

    public static final String RESERVE_CUSTOMER_BALANCE_FAILED = "RESERVE_CUSTOMER_BALANCE_FAILED";

    public static final String RESERVE_PRODUCT_STOCK_SUCCESSFULLY =
            "RESERVE_PRODUCT_STOCK_SUCCESSFULLY";

    public static final String RESERVE_PRODUCT_STOCK_FAILED = "RESERVE_PRODUCT_STOCK_FAILED";

    public static final String COMPENSATE_CUSTOMER_BALANCE = "COMPENSATE_CUSTOMER_BALANCE";

}
