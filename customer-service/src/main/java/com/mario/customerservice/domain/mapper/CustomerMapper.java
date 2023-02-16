package com.mario.customerservice.domain.mapper;

import com.mario.customerservice.domain.entity.CustomerEntity;
import com.mario.customerservice.model.Customer;
import com.mario.customerservice.model.requests.CustomerRequest;

public class CustomerMapper {

    private CustomerMapper() {

    }

    public static Customer toCustomer(CustomerEntity customerEntity) {
        return Customer.builder()
                .id(customerEntity.getId())
                .username(customerEntity.getUsername())
                .fullName(customerEntity.getFullName())
                .balance(customerEntity.getBalance())
                .build();
    }

    public static Customer toCustomer(CustomerRequest customerRequest) {
        return Customer.builder()
                .username(customerRequest.username())
                .fullName(customerRequest.fullName())
                .balance(customerRequest.balance())
                .build();
    }

    public static CustomerEntity toEntity(Customer customer) {
        return CustomerEntity.builder()
                .id(customer.getId())
                .username(customer.getUsername())
                .fullName(customer.getFullName())
                .balance(customer.getBalance())
                .build();
    }

}
