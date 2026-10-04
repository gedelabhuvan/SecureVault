package com.securevault.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securevault.backend.dto.SecurityEventResponse;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.service.SecurityEventService;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    private final SecurityEventService securityEventService;
    private final UserRepository userRepository;

    public SecurityController(
            SecurityEventService securityEventService,
            UserRepository userRepository
    ) {
        this.securityEventService = securityEventService;
        this.userRepository = userRepository;
    }

    // =========================
    // GET MY LOGIN HISTORY
    // =========================
    @GetMapping("/login-history")
    public ResponseEntity<List<SecurityEventResponse>> getLoginHistory(
            Authentication authentication
    ) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found"
                        )
                );

        List<SecurityEventResponse> history =
                securityEventService.getLoginHistory(
                        user.getId()
                );

        return ResponseEntity.ok(history);
    }
}