package com.mario.customerservice.controllers;

import com.mario.customerservice.model.requests.CustomerRequest;
import com.mario.customerservice.model.Customer;
import com.mario.customerservice.services.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Customer create(@RequestBody @Valid CustomerRequest customerRequest) {
        return customerService.create(customerRequest);
    }

    @GetMapping("/{customerId}")
    public Customer findById(@PathVariable UUID customerId) {
        return customerService.findById(customerId);
    }

}
