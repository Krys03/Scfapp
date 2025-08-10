package com.supplychain.scfapp.service;

import com.supplychain.scfapp.model.Role;
import com.supplychain.scfapp.model.User;
import com.supplychain.scfapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Invite un utilisateur :
     * - génère un mot de passe temporaire
     * - créé le compte avec le rôle demandé
     * - envoie un email d'invitation (fake ou réel selon EmailService)
     */
    public User inviteUser(String email, Role role) {
        if (userRepository.existsByUsername(email)) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà : " + email);
        }

        // Génère un mot de passe temporaire (12+ chars)
        String rawPassword = UUID.randomUUID().toString().replace("-", "") + "!";
        if (rawPassword.length() < 12) {
            rawPassword = rawPassword + "X".repeat(12 - rawPassword.length());
        }
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Crée l'utilisateur
        User user = new User();
        user.setUsername(email);
        user.setPassword(encodedPassword);
        user.setRoles(Set.of(role));
        userRepository.save(user);

        // Envoie l'invitation (le mail sera FAKE ou RÉEL selon EmailService)
        emailService.sendInvitationEmail(email, email, rawPassword);

        return user;
    }
}
