package com.supplychain.scfapp.service;

import com.supplychain.scfapp.model.RefreshToken;
import com.supplychain.scfapp.model.User;
import com.supplychain.scfapp.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration}")
    private long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepo;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepo) {
        this.refreshTokenRepo = refreshTokenRepo;
    }

    /**
     * Crée un nouveau refresh token en révoquant l'ancien (rotation)
     */
    public RefreshToken createRefreshToken(User user) {
        Optional<RefreshToken> oldOpt = refreshTokenRepo.findByUser(user);
        oldOpt.ifPresent(old -> {
            old.setRevoked(true);
            refreshTokenRepo.save(old);
        });

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        rt.setToken(UUID.randomUUID().toString());
        rt.setRevoked(false);
        return refreshTokenRepo.save(rt);
    }

    /**
     * Vérifie l'expiration ET le statut revoked
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) {
            refreshTokenRepo.delete(token);
            throw new RuntimeException("Refresh token has been revoked. Please login again.");
        }
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepo.delete(token);
            throw new RuntimeException("Refresh token expired. Please login again.");
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepo.findByToken(token);
    }

    /**
     * Révocation manuelle (logout)
     */
    public void revokeRefreshToken(User user) {
        refreshTokenRepo.findByUser(user)
            .ifPresent(rt -> {
                rt.setRevoked(true);
                refreshTokenRepo.save(rt);
            });
    }
}
