package com.supplychain.scfapp.service;

import com.supplychain.scfapp.model.PasswordResetToken;
import com.supplychain.scfapp.model.User;
import com.supplychain.scfapp.repository.PasswordResetTokenRepository;
import com.supplychain.scfapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PasswordResetService {

    @Value("${app.password-reset.expiration}")
    private long expirationMs;

    private final PasswordResetTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final JavaMailSender mailSender;

    public PasswordResetService(PasswordResetTokenRepository tokenRepo,
                                UserRepository userRepo,
                                JavaMailSender mailSender) {
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
        this.mailSender = mailSender;
    }

    @Transactional
    public void createAndSendToken(String email) {
        User user = userRepo.findByUsername(email)
                          .orElseThrow(() -> new RuntimeException("Email unknown"));
        tokenRepo.deleteByUserId(user.getId());

        PasswordResetToken prt = new PasswordResetToken();
        prt.setUser(user);
        prt.setToken(UUID.randomUUID().toString());
        prt.setExpiryDate(Instant.now().plusMillis(expirationMs));
        tokenRepo.save(prt);

        String resetLink = "http://localhost:8081/auth/reset-password?token=" + prt.getToken() +
                           "&newPassword=";
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Réinitialisation mot de passe SCF");
        msg.setText("Cliquez sur ce lien pour réinitialiser votre mot de passe :\n" + resetLink +
                    "\n\nEntrez votre nouveau mot de passe à la suite du lien.");
        mailSender.send(msg);
    }

    @Transactional
    public void resetPassword(String token, String newPassword, PasswordEncoder encoder) {
        PasswordResetToken prt = tokenRepo.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid reset token"));
        if (prt.getExpiryDate().isBefore(Instant.now())) {
            tokenRepo.delete(prt);
            throw new RuntimeException("Reset token expired");
        }
        User user = prt.getUser();
        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);
        tokenRepo.delete(prt);
    }
}
