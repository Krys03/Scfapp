package com.supplychain.scfapp.service;

import com.supplychain.scfapp.model.PasswordResetToken;
import com.supplychain.scfapp.model.User;
import com.supplychain.scfapp.repository.PasswordResetTokenRepository;
import com.supplychain.scfapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PasswordResetService {

    // Lit la valeur définie dans application.properties
    // ex: app.password-reset.expiration=3600000 (1 heure)
    @Value("${app.password-reset.expiration}")
    private long expirationMs;

    private final PasswordResetTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(PasswordResetTokenRepository tokenRepo,
                                UserRepository userRepo,
                                RefreshTokenService refreshTokenService,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /** Génère le token de reset et envoie le mail (fake ou réel selon EmailService). */
    @Transactional
    public void sendResetLink(String usernameOrEmail) {
        User user = userRepo.findByUsername(usernameOrEmail)
                .orElseThrow(() -> new RuntimeException("Email/username inconnu"));

        // Évite les doublons : supprime les anciens tokens de ce user
        tokenRepo.deleteByUserId(user.getId());

        PasswordResetToken prt = new PasswordResetToken();
        prt.setUser(user);
        prt.setToken(UUID.randomUUID().toString());
        prt.setExpiryDate(Instant.now().plusMillis(expirationMs));
        tokenRepo.save(prt);

        String resetLink = "http://localhost:8081/auth/reset-password?token=" + prt.getToken()
                + "&newPassword=";

        emailService.sendResetEmail(user.getUsername(), resetLink);
    }

    /** Applique le nouveau mot de passe + invalide anciens tokens. */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = tokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Reset token invalide"));

        if (prt.getExpiryDate().isBefore(Instant.now())) {
            tokenRepo.delete(prt);
            throw new RuntimeException("Reset token expiré");
        }

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(Instant.now());   // invalide immédiatement les anciens access tokens
        userRepo.save(user);

        tokenRepo.delete(prt);                      // consomme le token
        refreshTokenService.revokeRefreshToken(user); // révoque le refresh courant
    }
}
