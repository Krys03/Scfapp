package com.supplychain.scfapp.model;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token",
       indexes = {
           @Index(name = "ix_refresh_token_user", columnList = "user_id"),
           @Index(name = "ux_refresh_token_token", columnList = "token", unique = true)
       })
public class RefreshToken {

    @Id
    @GeneratedValue
    @UuidGenerator // Hibernate 6 : génère un UUID côté Java avant l'insert
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean revoked;

    // --- getters / setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }
}
