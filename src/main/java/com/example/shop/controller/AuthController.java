package com.example.shop.controller;

import com.example.shop.dto.RegisterRequest;
import com.example.shop.dto.UserResponse;
import com.example.shop.entity.Cart;
import com.example.shop.entity.Role;
import com.example.shop.entity.User;
import com.example.shop.exception.BadRequestException;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.repository.CartRepository;
import com.example.shop.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final CartRepository cartRepo;
    private final PasswordEncoder encoder;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepo.existsByEmail(email))
            throw new BadRequestException("Email already registered");

        User u = new User();
        u.setName(req.name().trim());
        u.setEmail(email);
        u.setPassword(encoder.encode(req.password()));
        u.setRole(Role.USER);
        userRepo.save(u);

        Cart cart = new Cart();
        cart.setUser(u);
        cartRepo.save(cart);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Registered successfully"));
    }

    /** Used by the frontend after login to learn the user's role. */
    @GetMapping("/me")
    public UserResponse me(Principal principal) {
        User u = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserResponse(u.getName(), u.getEmail(), u.getRole().name());
    }
}
