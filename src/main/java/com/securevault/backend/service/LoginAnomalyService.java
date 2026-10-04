package com.securevault.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.securevault.backend.entity.SecurityEvent;
import com.securevault.backend.repository.SecurityEventRepository;

@Service
public class LoginAnomalyService {

    private final SecurityEventRepository securityEventRepository;

    public LoginAnomalyService(
            SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    public boolean isAnomalousLogin(
            Long userId,
            String ipAddress,
            String userAgent) {

        LocalDateTime from = LocalDateTime.now().minusDays(30);
        LocalDateTime to = LocalDateTime.now();

        List<SecurityEvent> recentEvents =
                securityEventRepository
                        .findByUserIdAndEventTimeBetweenOrderByEventTimeDesc(
                                userId,
                                from,
                                to
                        );

        if (recentEvents.isEmpty()) {
            return false;
        }

        return recentEvents.stream().noneMatch(event ->
                equalsValue(event.getIpAddress(), ipAddress)
                &&
                equalsValue(event.getUserAgent(), userAgent)
        );
    }

    private boolean equalsValue(String first, String second) {

        if (first == null || second == null) {
            return false;
        }

        return first.equals(second);
    }
}