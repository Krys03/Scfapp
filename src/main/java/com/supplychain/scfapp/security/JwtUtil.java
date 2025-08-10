package com.supplychain.scfapp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMillis;
    private final String issuer;

    public JwtUtil(
            @Value("${app.jwt.secret:dev-change-me-please-32bytes-minimum-secret-key!!!}") String secret,
            @Value("${app.jwt.expiration:900000}") long expirationMillis,
            @Value("${app.jwt.issuer:scfapp}") String issuer
    ) {
        // Assure un minimum de 32 bytes pour HS256 en DEV
        if (secret == null) secret = "dev-change-me-please-32bytes-minimum-secret-key!!!";
        if (secret.length() < 32) {
            secret = (secret + "________________________________________________").substring(0, 32);
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
        this.issuer = issuer;
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .setSubject(username)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return getAllClaims(token).getSubject();
    }

    public Instant getIssuedAt(String token) {
        Date iat = getAllClaims(token).getIssuedAt();
        return iat != null ? iat.toInstant() : null;
    }

    public boolean isTokenValidSignature(String token) {
        return isTokenValid(token);
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }
}
