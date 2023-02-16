package com.mario.customerservice.services;

import com.mario.common.PlacedOrderEvent;
import com.mario.customerservice.domain.entity.CustomerEntity;
import com.mario.customerservice.domain.mapper.CustomerMapper;
import com.mario.customerservice.domain.exception.NotFoundException;
import com.mario.customerservice.model.requests.CustomerRequest;
import com.mario.customerservice.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerEntity findById(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(NotFoundException::new);
    }

    @Transactional
    public CustomerEntity create(CustomerRequest customerRequest) {
        var customer = CustomerMapper.toEntity(customerRequest);
        customer.setId(UUID.randomUUID());
        return customerRepository.save(customer);
    }

    @Transactional
    public boolean reserveBalance(PlacedOrderEvent orderEvent) {
        var customer = findById(orderEvent.customerId());
        if (customer
                .getBalance()
                .subtract(orderEvent.price().multiply(BigDecimal.valueOf(orderEvent.quantity())))
                .compareTo(BigDecimal.ZERO)
                < 0) {
            return false;
        }
        customer.setBalance(
                customer
                        .getBalance()
                        .subtract(orderEvent.price().multiply(BigDecimal.valueOf(orderEvent.quantity()))));
        customerRepository.save(customer);
        return true;
    }

    @Transactional
    public void compensateBalance(PlacedOrderEvent orderEvent) {
        var customer = findById(orderEvent.customerId());
        customer.setBalance(
                customer
                        .getBalance()
                        .add(orderEvent.price().multiply(BigDecimal.valueOf(orderEvent.quantity()))));
        customerRepository.save(customer);
    }

}
