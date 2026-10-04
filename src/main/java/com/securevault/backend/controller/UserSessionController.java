package com.securevault.backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.securevault.backend.entity.User;
import com.securevault.backend.entity.UserSession;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.service.UserSessionService;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "http://localhost:5173")
public class UserSessionController {

    private final UserSessionService userSessionService;
    private final UserRepository userRepository;

    public UserSessionController(
            UserSessionService userSessionService,
            UserRepository userRepository
    ) {
        this.userSessionService = userSessionService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getActiveSessions(
            Authentication authentication
    ) {
        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow();

        List<UserSessionResponse> sessions =
                userSessionService.getActiveSessions(user.getId())
                        .stream()
                        .map(UserSessionResponse::new)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> revokeSession(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {
        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow();

        userSessionService.revokeSession(
                sessionId,
                user.getId()
        );

        return ResponseEntity.ok(
                java.util.Map.of(
                        "message",
                        "Session revoked successfully."
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<?> revokeAllSessions(
            Authentication authentication
    ) {
        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow();

        userSessionService.revokeAllSessions(user.getId());

        return ResponseEntity.ok(
                java.util.Map.of(
                        "message",
                        "All sessions revoked successfully."
                )
        );
    }

    public static class UserSessionResponse {

        private final Long id;
        private final String createdAt;
        private final String expiresAt;
        private final String ipAddress;
        private final String userAgent;
        private final boolean revoked;

        public UserSessionResponse(UserSession session) {
            this.id = session.getId();
            this.createdAt = session.getCreatedAt().toString();
            this.expiresAt = session.getExpiresAt().toString();
            this.ipAddress = session.getIpAddress();
            this.userAgent = session.getUserAgent();
            this.revoked = session.isRevoked();
        }

        public Long getId() {
            return id;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getExpiresAt() {
            return expiresAt;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public boolean isRevoked() {
            return revoked;
        }
    }
}