package com.example.shop.dto;

import java.math.BigDecimal;

public record OrderItemResponse(Long productId, String productName, Integer quantity, BigDecimal price) {
}
