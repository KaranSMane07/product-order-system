package com.example.shop.dto;

import java.math.BigDecimal;

public record ProductResponse(Long id, String name, String description,
                              BigDecimal price, Integer quantity, boolean enabled) {
}
