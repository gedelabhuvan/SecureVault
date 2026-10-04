package com.securevault.backend.controller;

import com.securevault.backend.entity.SecurityAlert;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.service.SecurityAlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/security/alerts")
public class SecurityAlertController {

    private final SecurityAlertService securityAlertService;
    private final UserRepository userRepository;

    public SecurityAlertController(
            SecurityAlertService securityAlertService,
            UserRepository userRepository) {

        this.securityAlertService = securityAlertService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<SecurityAlert> createAlert(
            @RequestParam String alertType,
            @RequestParam String message,
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        SecurityAlert alert =
                securityAlertService.createAlert(
                        user,
                        alertType,
                        message
                );

        return ResponseEntity.ok(alert);
    }

    @PutMapping("/{alertId}/resolve")
    public ResponseEntity<String> resolveAlert(
            @PathVariable Long alertId) {

        securityAlertService.resolveAlert(alertId);

        return ResponseEntity.ok(
                "Security alert resolved successfully"
        );
    }
}
