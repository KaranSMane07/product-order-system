package com.example.shop.service;

import com.example.shop.dto.ProductRequest;
import com.example.shop.dto.ProductResponse;
import com.example.shop.dto.ProductUpdateRequest;
import com.example.shop.entity.Product;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repo;

    private ProductResponse map(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(),
                p.getPrice(), p.getQuantity(), p.isEnabled());
    }

    private Product find(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    @Transactional
    public ProductResponse add(ProductRequest r) {
        Product p = new Product();
        p.setName(r.name());
        p.setDescription(r.description());
        p.setPrice(r.price());
        p.setQuantity(r.quantity());
        p.setEnabled(true);
        return map(repo.save(p));
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest r) {
        Product p = find(id);
        if (r.price() != null) p.setPrice(r.price());
        if (r.quantity() != null) p.setQuantity(r.quantity());
        return map(repo.save(p));
    }

    @Transactional
    public ProductResponse setEnabled(Long id, boolean enabled) {
        Product p = find(id);
        p.setEnabled(enabled);
        return map(repo.save(p));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listEnabled() {
        return repo.findByEnabledTrueOrderByIdAsc().stream().map(this::map).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listAll() {
        return repo.findAll().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .map(this::map).toList();
    }
}
