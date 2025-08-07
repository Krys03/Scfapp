package com.supplychain.scfapp.controller;

import com.supplychain.scfapp.dto.ChangePasswordRequest;
import com.supplychain.scfapp.model.RefreshToken;
import com.supplychain.scfapp.model.User;
import com.supplychain.scfapp.repository.UserRepository;
import com.supplychain.scfapp.security.JwtUtil;
import com.supplychain.scfapp.service.PasswordResetService;
import com.supplychain.scfapp.service.RefreshTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

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
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        Authentication authentication = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );

        String accessToken = jwtUtil.generateToken(username);
        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(Map.of(
            "accessToken", accessToken,
            "refreshToken", refreshToken.getToken()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String requestToken = request.get("refreshToken");
        try {
            RefreshToken stored = refreshTokenService.findByToken(requestToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
            refreshTokenService.verifyExpiration(stored);

            String newAccessToken = jwtUtil.generateToken(stored.getUser().getUsername());
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (RuntimeException ex) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
        }
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
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Ancien mot de passe incorrect"));
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepo.save(user);

        return ResponseEntity.ok(Map.of("message", "Mot de passe changé avec succès"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            passwordResetService.createAndSendToken(email);
            return ResponseEntity.ok(Map.of("message", "Reset link sent to email"));
        } catch (RuntimeException ex) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token,
                                           @RequestParam String newPassword) {
        try {
            passwordResetService.resetPassword(token, newPassword, passwordEncoder);
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (RuntimeException ex) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
        }
    }
}
