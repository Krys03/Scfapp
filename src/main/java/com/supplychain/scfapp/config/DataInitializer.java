package com.supplychain.scfapp.config;

import com.supplychain.scfapp.model.Role;
import com.supplychain.scfapp.model.User;
import com.supplychain.scfapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {

            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("adminpass"));
                admin.setRoles(Set.of(Role.ADMIN));
                userRepository.save(admin);
                System.out.println("✅ Admin created");
            }

            if (userRepository.findByUsername("supplier1").isEmpty()) {
                User supplier = new User();
                supplier.setUsername("supplier1");
                supplier.setPassword(passwordEncoder.encode("supppass"));
                supplier.setRoles(Set.of(Role.SUPPLIER));
                userRepository.save(supplier);
                System.out.println("✅ Supplier created");
            }

            if (userRepository.findByUsername("buyer1").isEmpty()) {
                User buyer = new User();
                buyer.setUsername("buyer1");
                buyer.setPassword(passwordEncoder.encode("buypass"));
                buyer.setRoles(Set.of(Role.BUYER));
                userRepository.save(buyer);
                System.out.println("✅ Buyer created");
            }
        };
    }
}
