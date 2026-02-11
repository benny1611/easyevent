package com.benny1611.easyevent.dao;

import com.benny1611.easyevent.entity.PasswordResetToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t FROM PasswordResetToken t
        WHERE t.id = :id
          AND t.used = false
          AND t.expiresAt > :now
    """)
    Optional<PasswordResetToken> findForUpdate(
            UUID id,
            Instant now
    );

    @Modifying
    @Query("""
            DELETE FROM PasswordResetToken t
            WHERE t.used = true
            OR t.expiresAt < :now
            """)
    void deleteExpiredOrUsed(Instant now);

    List<PasswordResetToken> findByUsedFalseAndExpiresAtAfter(Instant now);
}
