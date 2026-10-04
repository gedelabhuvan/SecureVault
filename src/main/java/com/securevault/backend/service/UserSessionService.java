package com.securevault.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.securevault.backend.entity.User;
import com.securevault.backend.entity.UserSession;
import com.securevault.backend.repository.UserSessionRepository;

@Service
public class UserSessionService {

    private final UserSessionRepository userSessionRepository;

    public UserSessionService(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    @Transactional
    public UserSession createSession(
            User user,
            String sessionToken,
            String ipAddress,
            String userAgent
    ) {
        UserSession session = new UserSession();

        session.setUser(user);
        session.setSessionToken(sessionToken);
        session.setExpiresAt(LocalDateTime.now().plusDays(1));
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setRevoked(false);

        return userSessionRepository.save(session);
    }

    public List<UserSession> getActiveSessions(Long userId) {
        return userSessionRepository
                .findByUserIdAndRevokedFalseOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void revokeSession(Long sessionId, Long userId) {
        userSessionRepository.findById(sessionId).ifPresent(session -> {
            if (session.getUser().getId().equals(userId)) {
                session.setRevoked(true);
                userSessionRepository.save(session);
            }
        });
    }

    @Transactional
    public void revokeAllSessions(Long userId) {
        List<UserSession> sessions =
                userSessionRepository
                        .findByUserIdAndRevokedFalseOrderByCreatedAtDesc(userId);

        for (UserSession session : sessions) {
            session.setRevoked(true);
        }

        userSessionRepository.saveAll(sessions);
    }

    @Transactional
    public void deleteExpiredSessions() {
        userSessionRepository
                .deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
