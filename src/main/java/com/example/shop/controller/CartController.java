package com.example.shop.controller;

import com.example.shop.dto.CartItemRequest;
import com.example.shop.dto.CartItemResponse;
import com.example.shop.dto.CartItemUpdateRequest;
import com.example.shop.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService service;

    @GetMapping
    public List<CartItemResponse> view(Principal p) {
        return service.view(p.getName());
    }

    @PostMapping("/items")
    public List<CartItemResponse> add(Principal p, @Valid @RequestBody CartItemRequest r) {
        return service.addItem(p.getName(), r);
    }

    @PutMapping("/items/{itemId}")
    public List<CartItemResponse> update(Principal p, @PathVariable Long itemId,
                                         @Valid @RequestBody CartItemUpdateRequest r) {
        return service.updateItem(p.getName(), itemId, r.quantity());
    }

    @DeleteMapping("/items/{itemId}")
    public List<CartItemResponse> remove(Principal p, @PathVariable Long itemId) {
        return service.removeItem(p.getName(), itemId);
    }
}
