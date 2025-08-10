package com.supplychain.scfapp.controller;

import com.supplychain.scfapp.dto.ChangePasswordRequest;
import com.supplychain.scfapp.model.RefreshToken;
import com.supplychain.scfapp.model.User;
import com.supplychain.scfapp.repository.UserRepository;
import com.supplychain.scfapp.security.JwtUtil;
import com.supplychain.scfapp.service.PasswordResetService;
import com.supplychain.scfapp.service.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthenticationManager authManager,
                          JwtUtil jwtUtil,
                          UserRepository userRepo,
                          RefreshTokenService refreshTokenService,
                          PasswordEncoder passwordEncoder,
                          PasswordResetService passwordResetService) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        log.info("🔐 Tentative d'authentification avec : {}", username);

        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Bad credentials"));
        }

        String accessToken = jwtUtil.generateToken(username);
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("✅ Utilisateur trouvé : {} | Rôles : {}", user.getUsername(), user.getRoles());

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken.getToken()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String provided = request.get("refreshToken");
        return refreshTokenService.findByTokenWithUser(provided) // ✅ charge user (join fetch)
                .map(rt -> {
                    if (rt.isRevoked()) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("error", "Refresh token revoked"));
                    }
                    if (rt.getExpiryDate().isBefore(Instant.now())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("error", "Refresh token expired"));
                    }
                    String newAccess = jwtUtil.generateToken(rt.getUser().getUsername());
                    return ResponseEntity.ok(Map.of("accessToken", newAccess));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid refresh token")));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody Map<String, String> request) {
        String requestToken = request.get("refreshToken");
        refreshTokenService.findByToken(requestToken)
                .ifPresent(rt -> refreshTokenService.revokeRefreshToken(rt.getUser()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(Authentication authentication,
                                            @RequestBody ChangePasswordRequest dto) {
        String username = authentication.getName();
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Ancien mot de passe incorrect"));
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setPasswordChangedAt(Instant.now());
        userRepo.save(user);

        // Invalidation des refresh tokens existants
        refreshTokenService.revokeRefreshToken(user);
        return ResponseEntity.ok(Map.of("message", "Mot de passe changé avec succès"));
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> req) {
        String usernameOrEmail = req.get("usernameOrEmail");
        passwordResetService.sendResetLink(usernameOrEmail);
        return ResponseEntity.ok(Map.of("message", "If the user exists, a reset link has been sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token,
                                           @RequestParam String newPassword) {
        try {
            passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }
}
