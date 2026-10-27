package com.example.shop.controller;

import com.example.shop.dto.OrderResponse;
import com.example.shop.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    @PostMapping
    public ResponseEntity<OrderResponse> place(Principal p) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.placeOrder(p.getName()));
    }

    @GetMapping
    public List<OrderResponse> mine(Principal p) {
        return service.myOrders(p.getName());
    }
}
