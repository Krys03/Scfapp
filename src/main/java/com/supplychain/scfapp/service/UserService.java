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

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public User inviteUser(String email, Role role) {
        // Génère un mot de passe aléatoire (8 caractères)
        String rawPassword = UUID.randomUUID().toString().substring(0, 8);
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Crée l'utilisateur
        User user = new User();
        user.setUsername(email);
        user.setPassword(encodedPassword);
        user.setRoles(Set.of(role));

        userRepository.save(user);

        // Envoie l'invitation par email
        emailService.sendInvitationEmail(email, email, rawPassword);

        return user;
    }
}
