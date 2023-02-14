package com.mario.inventoryservice.domain;

import jakarta.validation.constraints.NotBlank;

public record ProductRequest(@NotBlank String name, int stocks) {

}
