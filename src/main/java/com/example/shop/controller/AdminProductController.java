package com.example.shop.controller;

import com.example.shop.dto.ProductRequest;
import com.example.shop.dto.ProductResponse;
import com.example.shop.dto.ProductUpdateRequest;
import com.example.shop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService service;

    @GetMapping
    public List<ProductResponse> listAll() {
        return service.listAll();
    }

    @PostMapping
    public ResponseEntity<ProductResponse> add(@Valid @RequestBody ProductRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(r));
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest r) {
        return service.update(id, r);
    }

    @PatchMapping("/{id}/enable")
    public ProductResponse enable(@PathVariable Long id) {
        return service.setEnabled(id, true);
    }

    @PatchMapping("/{id}/disable")
    public ProductResponse disable(@PathVariable Long id) {
        return service.setEnabled(id, false);
    }
}
