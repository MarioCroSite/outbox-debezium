package com.mario.customerservice.domain.port;

import com.mario.customerservice.domain.CustomerRequest;
import com.mario.customerservice.domain.PlacedOrderEvent;
import com.mario.customerservice.domain.entity.Customer;

import java.util.UUID;

public interface CustomerUseCasePort {

    Customer findById(UUID customerId);

    Customer create(CustomerRequest customerRequest);

    boolean reserveBalance(PlacedOrderEvent orderEvent);

    void compensateBalance(PlacedOrderEvent orderEvent);
}
