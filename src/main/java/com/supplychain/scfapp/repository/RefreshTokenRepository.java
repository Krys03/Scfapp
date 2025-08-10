package com.supplychain.scfapp.repository;

import com.supplychain.scfapp.model.RefreshToken;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    // Conserve la méthode simple si tu en as besoin ailleurs
    Optional<RefreshToken> findByToken(String token);

    // ✅ Charge aussi l'utilisateur pour éviter LazyInitializationException
    @Query("select r from RefreshToken r join fetch r.user where r.token = :token")
    Optional<RefreshToken> findByTokenFetchUser(@Param("token") String token);

    @Modifying
    @Transactional
    @Query("update RefreshToken r set r.revoked = true where r.user.id = :userId and r.revoked = false")
    int revokeAllByUserId(@Param("userId") Long userId);
}
