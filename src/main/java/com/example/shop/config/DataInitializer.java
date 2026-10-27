package com.example.shop.config;

import com.example.shop.entity.Product;
import com.example.shop.entity.Role;
import com.example.shop.entity.User;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Seeds a default admin and a few demo products on first start. */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (!userRepo.existsByEmail("admin@shop.com")) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@shop.com");
            admin.setPassword(encoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            userRepo.save(admin);
        }
        if (productRepo.count() == 0) {
            productRepo.save(product("Mechanical Keyboard", "Tenkeyless, blue switches", "1999.00", 10));
            productRepo.save(product("Wireless Mouse", "2.4 GHz, silent click", "799.00", 25));
            productRepo.save(product("USB-C Hub", "7-in-1 with HDMI", "1499.00", 5));
        }
    }

    private Product product(String name, String desc, String price, int qty) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(new BigDecimal(price));
        p.setQuantity(qty);
        p.setEnabled(true);
        return p;
    }
}
