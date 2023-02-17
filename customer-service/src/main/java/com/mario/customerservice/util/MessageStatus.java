package com.mario.customerservice.util;

public class MessageStatus {

    private MessageStatus() {

    }

    public static final String CUSTOMER = "CUSTOMER";

    public static final String ORDER_CREATED = "ORDER_CREATED";

    public static final String COMPENSATE_CUSTOMER_BALANCE = "COMPENSATE_CUSTOMER_BALANCE";

    public static final String RESERVE_CUSTOMER_BALANCE_FAILED = "RESERVE_CUSTOMER_BALANCE_FAILED";

    public static final String RESERVE_CUSTOMER_BALANCE_SUCCESSFULLY =
            "RESERVE_CUSTOMER_BALANCE_SUCCESSFULLY";

    public static final String EVENT_TYPE = "eventType";
}
