package com.example.shop.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

/** Both fields are optional: send only what you want to change. */
public record ProductUpdateRequest(
        @DecimalMin(value = "0.01", message = "Price must be greater than 0") BigDecimal price,
        @Min(value = 0, message = "Quantity cannot be negative") Integer quantity) {
}
