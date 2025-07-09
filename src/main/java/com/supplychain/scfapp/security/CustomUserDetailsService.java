package com.supplychain.scfapp.security;

import com.supplychain.scfapp.model.User;
import com.supplychain.scfapp.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository repo) {
        this.userRepository = repo;
        System.out.println("🟢 CustomUserDetailsService initialisé !");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔐 Tentative d'authentification avec : " + username);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                System.out.println("❌ Utilisateur non trouvé : " + username);
                return new UsernameNotFoundException("Utilisateur non trouvé : " + username);
            });

        System.out.println("✅ Utilisateur trouvé : " + user.getUsername() + " | Rôles : " + user.getRoles());

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList())
        );
    }
}
