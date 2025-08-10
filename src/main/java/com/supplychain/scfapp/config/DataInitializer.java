package com.supplychain.scfapp.config;

import com.supplychain.scfapp.model.Role;
import com.supplychain.scfapp.model.User;
import com.supplychain.scfapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Set;

@Configuration
public class DataInitializer {

    // ⚙️ Toggles simples (tu peux aussi les passer en properties, voir plus bas)
    private boolean seedUsers = true;                // crée admin/buyer1/supplier1 s'ils n'existent pas
    private boolean resetAdminOnBoot = true;        // remet le mdp de l'admin à chaque run (utile en dev)
    private String defaultPassword = "adminpass";   // mdp par défaut pour la seed

    @Bean
    CommandLineRunner initUsers(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (!seedUsers) return;

            // --- ADMIN ---
            users.findByUsername("admin").ifPresentOrElse(u -> {
                if (resetAdminOnBoot) {
                    u.setPassword(encoder.encode(defaultPassword));
                    u.setPasswordChangedAt(Instant.now());
                    if (u.getRoles() == null || u.getRoles().isEmpty()) {
                        u.setRoles(Set.of(Role.ADMIN));
                    }
                    users.save(u);
                    System.out.println("✅ Admin reset -> username=admin / password=" + defaultPassword);
                } else {
                    System.out.println("ℹ️ Admin existant conservé (aucune modification).");
                }
            }, () -> {
                User u = new User();
                u.setUsername("admin");
                u.setPassword(encoder.encode(defaultPassword));
                u.setRoles(Set.of(Role.ADMIN));
                u.setPasswordChangedAt(Instant.now());
                users.save(u);
                System.out.println("✅ Admin created -> username=admin / password=" + defaultPassword);
            });

            // --- BUYER (créé uniquement s'il n'existe pas) ---
            users.findByUsername("buyer1").ifPresentOrElse(u -> {
                System.out.println("ℹ️ Buyer existant conservé : buyer1");
            }, () -> {
                User u = new User();
                u.setUsername("buyer1");
                u.setPassword(encoder.encode(defaultPassword));
                u.setRoles(Set.of(Role.BUYER));
                u.setPasswordChangedAt(Instant.now());
                users.save(u);
                System.out.println("✅ Buyer created -> username=buyer1 / password=" + defaultPassword);
            });

            // --- SUPPLIER (créé uniquement s'il n'existe pas) ---
            users.findByUsername("supplier1").ifPresentOrElse(u -> {
                System.out.println("ℹ️ Supplier existant conservé : supplier1");
            }, () -> {
                User u = new User();
                u.setUsername("supplier1");
                u.setPassword(encoder.encode(defaultPassword));
                u.setRoles(Set.of(Role.SUPPLIER));
                u.setPasswordChangedAt(Instant.now());
                users.save(u);
                System.out.println("✅ Supplier created -> username=supplier1 / password=" + defaultPassword);
            });
        };
    }
}
