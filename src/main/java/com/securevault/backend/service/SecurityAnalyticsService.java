package com.securevault.backend.service;

import com.securevault.backend.entity.SecurityEvent;
import com.securevault.backend.repository.SecurityEventRepository;
import org.springframework.stereotype.Service;
import com.securevault.backend.repository.SecurityAlertRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecurityAnalyticsService {

    private final SecurityEventRepository securityEventRepository;
    private final SecurityAlertRepository securityAlertRepository;

    public SecurityAnalyticsService(
        SecurityEventRepository securityEventRepository,
        SecurityAlertRepository securityAlertRepository) {

    this.securityEventRepository = securityEventRepository;
    this.securityAlertRepository = securityAlertRepository;
}

    public long getSuccessfulLogins(
            Long userId,
            LocalDateTime from,
            LocalDateTime to) {

        return securityEventRepository
                .countByUserIdAndEventTypeAndLoginStatusAndEventTimeBetween(
                        userId,
                        SecurityEvent.EventType.LOGIN,
                        SecurityEvent.LoginStatus.SUCCESS,
                        from,
                        to
                );
    }

    public long getFailedLogins(
            Long userId,
            LocalDateTime from,
            LocalDateTime to) {

        return securityEventRepository
                .countByUserIdAndEventTypeAndLoginStatusAndEventTimeBetween(
                        userId,
                        SecurityEvent.EventType.LOGIN,
                        SecurityEvent.LoginStatus.FAILURE,
                        from,
                        to
                );
    }

public double getLoginSuccessRate(
        Long userId,
        LocalDateTime from,
        LocalDateTime to) {

    long successful = getSuccessfulLogins(userId, from, to);
    long failed = getFailedLogins(userId, from, to);

    long total = successful + failed;

    if (total == 0) {
        return 0.0;
    }

    return (successful * 100.0) / total;
}

public double getLoginFailureRate(
        Long userId,
        LocalDateTime from,
        LocalDateTime to) {

    long successful = getSuccessfulLogins(userId, from, to);
    long failed = getFailedLogins(userId, from, to);

    long total = successful + failed;

    if (total == 0) {
        return 0.0;
    }

    return (failed * 100.0) / total;
}

public long getSuspiciousActivities(
        Long userId,
        LocalDateTime from,
        LocalDateTime to) {

    return securityEventRepository
            .countByUserIdAndEventTypeAndLoginStatusAndEventTimeBetween(
                    userId,
                    SecurityEvent.EventType.LOGIN,
                    SecurityEvent.LoginStatus.FAILURE,
                    from,
                    to
            );
}


    public List<SecurityEvent> getRecentActivity(
            Long userId,
            LocalDateTime from,
            LocalDateTime to) {

        return securityEventRepository
                .findTop10ByUserIdAndEventTimeBetweenOrderByEventTimeDesc(
                        userId,
                        from,
                        to
                );
    }
    public long getActiveSecurityAlerts(Long userId) {

    return securityAlertRepository.countByUserIdAndStatus(
            userId,
            com.securevault.backend.entity.SecurityAlert.AlertStatus.ACTIVE
    );
}
}