package com.mario.orderservice.controllers;

import com.mario.orderservice.model.requests.OrderRequest;
import com.mario.orderservice.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void placeOrder(@RequestBody @Valid OrderRequest orderRequest) {
        log.info("Received new order request {}", orderRequest);
        orderService.placeOrder(orderRequest);
    }

}
