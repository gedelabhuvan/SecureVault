package com.securevault.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SecurityAnalyticsResponse {

    private long successfulLogins;
    private long failedLogins;
    private long totalLogins;

    private long suspiciousActivities;
    private long activeSecurityAlerts;

    private double loginSuccessRate;
    private double loginFailureRate;

    private LocalDateTime from;
    private LocalDateTime to;

    private List<SecurityEventResponse> recentActivity;

    public SecurityAnalyticsResponse(
            long successfulLogins,
            long failedLogins,
            long totalLogins,
            long suspiciousActivities,
            long activeSecurityAlerts,
            double loginSuccessRate,
            double loginFailureRate,
            LocalDateTime from,
            LocalDateTime to,
            List<SecurityEventResponse> recentActivity
    ) {
        this.successfulLogins = successfulLogins;
        this.failedLogins = failedLogins;
        this.totalLogins = totalLogins;
        this.suspiciousActivities = suspiciousActivities;
        this.activeSecurityAlerts = activeSecurityAlerts;
        this.loginSuccessRate = loginSuccessRate;
        this.loginFailureRate = loginFailureRate;
        this.from = from;
        this.to = to;
        this.recentActivity = recentActivity;
    }

    public long getSuccessfulLogins() {
        return successfulLogins;
    }

    public long getFailedLogins() {
        return failedLogins;
    }

    public long getTotalLogins() {
        return totalLogins;
    }

    public long getSuspiciousActivities() {
        return suspiciousActivities;
    }
    public long getActiveSecurityAlerts() {
        return activeSecurityAlerts;
    }

    public double getLoginSuccessRate() {
        return loginSuccessRate;
    }

    public double getLoginFailureRate() {
        return loginFailureRate;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public List<SecurityEventResponse> getRecentActivity() {
        return recentActivity;
    }
}