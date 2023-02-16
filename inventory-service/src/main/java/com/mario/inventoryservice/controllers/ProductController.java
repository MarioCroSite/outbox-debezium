package com.mario.inventoryservice.controllers;

import com.mario.inventoryservice.domain.mapper.ProductMapper;
import com.mario.inventoryservice.model.requests.ProductRequest;
import com.mario.inventoryservice.model.Product;
import com.mario.inventoryservice.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestBody @Valid ProductRequest productRequest) {
        log.info("Create new product {}", productRequest);
        return ProductMapper.toProduct(productService.create(productRequest));
    }

    @GetMapping("/{productId}")
    public Product findById(@PathVariable UUID productId) {
        return ProductMapper.toProduct(productService.findById(productId));
    }

}
