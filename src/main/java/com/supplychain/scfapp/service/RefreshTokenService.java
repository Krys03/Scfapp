package com.supplychain.scfapp.service;

import com.supplychain.scfapp.model.RefreshToken;
import com.supplychain.scfapp.model.User;
import com.supplychain.scfapp.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration:604800000}") // 7 jours par défaut
    private long refreshExpirationMs;

    private final RefreshTokenRepository repo;

    public RefreshTokenService(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Option : révoquer tous les anciens refresh de cet utilisateur
        repo.revokeAllByUserId(user.getId());

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(UUID.randomUUID().toString());
        rt.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        rt.setRevoked(false);
        return repo.save(rt);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByTokenWithUser(String token) {
        return repo.findByTokenFetchUser(token);
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return repo.findByToken(token);
    }

    @Transactional
    public void revokeRefreshToken(User user) {
        repo.revokeAllByUserId(user.getId());
    }

    @Transactional
    public void revokeTokenString(String token) {
        repo.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            repo.save(rt);
        });
    }
}
