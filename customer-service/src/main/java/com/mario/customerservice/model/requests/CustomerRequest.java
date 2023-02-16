package com.mario.customerservice.model.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CustomerRequest(@NotBlank String username, @NotBlank String fullName,
                              @NotNull BigDecimal balance) {

}
