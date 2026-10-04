package com.securevault.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securevault.backend.entity.UserSession;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findBySessionToken(String sessionToken);

    List<UserSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<UserSession> findByUserIdAndRevokedFalseOrderByCreatedAtDesc(Long userId);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}