package com.securevault.backend.controller;

import com.securevault.backend.dto.SecurityAnalyticsResponse;
import com.securevault.backend.dto.SecurityEventResponse;
import com.securevault.backend.entity.SecurityEvent;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.service.SecurityAnalyticsService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/security")
public class SecurityAnalyticsController {

    private final SecurityAnalyticsService securityAnalyticsService;
    private final UserRepository userRepository;

    public SecurityAnalyticsController(
            SecurityAnalyticsService securityAnalyticsService,
            UserRepository userRepository
    ) {
        this.securityAnalyticsService = securityAnalyticsService;
        this.userRepository = userRepository;
    }

    @GetMapping("/analytics")
    public ResponseEntity<SecurityAnalyticsResponse> getAnalytics(
            Authentication authentication,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found"
                        )
                );

        LocalDate endDate = to != null ? to : LocalDate.now();
        LocalDate startDate = from != null ? from : endDate.minusDays(6);

        LocalDateTime fromDateTime = startDate.atStartOfDay();
        LocalDateTime toDateTime = endDate.plusDays(1).atStartOfDay();

        long successfulLogins =
                securityAnalyticsService.getSuccessfulLogins(
                        user.getId(),
                        fromDateTime,
                        toDateTime
                );

        long failedLogins =
                securityAnalyticsService.getFailedLogins(
                        user.getId(),
                        fromDateTime,
                        toDateTime
                );

        long suspiciousActivities =
        securityAnalyticsService.getSuspiciousActivities(
                user.getId(),
                fromDateTime,
                toDateTime
        );
        long activeSecurityAlerts =
                securityAnalyticsService.getActiveSecurityAlerts(
                        user.getId()
                );

        List<SecurityEvent> events =
                securityAnalyticsService.getRecentActivity(
                        user.getId(),
                        fromDateTime,
                        toDateTime
                );

        List<SecurityEventResponse> recentActivity =
                events.stream()
                        .map(event -> new SecurityEventResponse(
                                event.getId(),
                                event.getEventType().name(),
                                event.getLoginStatus(),
                                event.getEventTime(),
                                event.getIpAddress(),
                                event.getUserAgent()
                        ))
                        .toList();
double loginSuccessRate =
        securityAnalyticsService.getLoginSuccessRate(
                user.getId(),
                fromDateTime,
                toDateTime
        );

double loginFailureRate =
        securityAnalyticsService.getLoginFailureRate(
                user.getId(),
                fromDateTime,
                toDateTime
        );

        
        SecurityAnalyticsResponse response =
        new SecurityAnalyticsResponse(
                successfulLogins,
                failedLogins,
                successfulLogins + failedLogins,
                suspiciousActivities,
                activeSecurityAlerts,
                loginSuccessRate,
                loginFailureRate,
                fromDateTime,
                toDateTime,
                recentActivity
        );

        return ResponseEntity.ok(response);
    }
}