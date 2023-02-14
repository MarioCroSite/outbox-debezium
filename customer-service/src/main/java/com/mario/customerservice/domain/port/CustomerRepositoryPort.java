package com.mario.customerservice.domain.port;

import com.mario.customerservice.domain.entity.Customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepositoryPort {

    Optional<Customer> findCustomerById(UUID customerId);

    Customer saveCustomer(Customer customer);
}
