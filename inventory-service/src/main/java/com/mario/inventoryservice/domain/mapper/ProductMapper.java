package com.mario.inventoryservice.domain.mapper;

import com.mario.inventoryservice.domain.entity.ProductEntity;
import com.mario.inventoryservice.model.Product;
import com.mario.inventoryservice.model.requests.ProductRequest;

public class ProductMapper {

    private ProductMapper() {

    }

    public static Product toProduct(ProductRequest productRequest) {
        return Product.builder()
                .name(productRequest.name())
                .stocks(productRequest.stocks())
                .build();
    }

    public static ProductEntity toEntity(Product product) {
        return ProductEntity.builder()
                .id(product.getId())
                .name(product.getName())
                .stocks(product.getStocks())
                .build();
    }

    public static ProductEntity toEntity(ProductRequest productRequest) {
        return ProductEntity.builder()
                .name(productRequest.name())
                .stocks(productRequest.stocks())
                .build();
    }

    public static Product toProduct(ProductEntity productEntity) {
        return Product.builder()
                .id(productEntity.getId())
                .name(productEntity.getName())
                .stocks(productEntity.getStocks())
                .build();
    }

}
