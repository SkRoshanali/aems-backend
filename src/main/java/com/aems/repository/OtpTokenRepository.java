package com.aems.repository;

import com.aems.entity.OtpToken;
import com.aems.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByUserAndOtpCodeAndIsUsedFalseAndExpiresAtAfter(
        User user, String otpCode, LocalDateTime now
    );
}
