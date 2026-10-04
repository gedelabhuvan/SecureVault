package com.securevault.backend.dto;

import java.time.LocalDateTime;

import com.securevault.backend.entity.SecurityEvent.LoginStatus;

public class SecurityEventResponse {

    private Long id;
    private String eventType;
    private LoginStatus loginStatus;
    private LocalDateTime eventTime;
    private String ipAddress;
    private String userAgent;

    public SecurityEventResponse(
            Long id,
            String eventType,
            LoginStatus loginStatus,
            LocalDateTime eventTime,
            String ipAddress,
            String userAgent
    ) {
        this.id = id;
        this.eventType = eventType;
        this.loginStatus = loginStatus;
        this.eventTime = eventTime;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public Long getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public LoginStatus getLoginStatus() {
        return loginStatus;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }
}