package com.benny1611.easyevent.dao;

import com.benny1611.easyevent.entity.PasswordResetToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT t FROM PasswordResetToken t
            WHERE t.used = false
            AND t.expiresAt > :now
            """)
    List<PasswordResetToken> findValidTokensForUpdate(Instant now);

    List<PasswordResetToken> findByUsedFalseAndExpiresAtAfter(Instant now);
}
