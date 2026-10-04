package com.securevault.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.securevault.backend.dto.SecurityEventResponse;
import com.securevault.backend.entity.SecurityEvent;
import com.securevault.backend.entity.SecurityEvent.EventType;
import com.securevault.backend.entity.SecurityEvent.LoginStatus;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.SecurityEventRepository;




@Service
public class SecurityEventService {

    private final SecurityEventRepository securityEventRepository;
    private final DeviceService deviceService;
    private final LoginAnomalyService loginAnomalyService;

    public SecurityEventService(
            SecurityEventRepository securityEventRepository,
            DeviceService deviceService,
            LoginAnomalyService loginAnomalyService
    ) {
        this.securityEventRepository = securityEventRepository;
        this.deviceService = deviceService;
        this.loginAnomalyService = loginAnomalyService;
    }

    // =========================
    // RECORD LOGIN EVENT
    // =========================
   public void recordLoginEvent(
        User user,
        LoginStatus loginStatus,
        String ipAddress,
        String userAgent
) {

    SecurityEvent event = new SecurityEvent();

    event.setUser(user);
    event.setEventType(EventType.LOGIN);
    event.setLoginStatus(loginStatus);
    event.setIpAddress(ipAddress);
    event.setUserAgent(userAgent);
    boolean anomalous = false;

if (user != null && loginStatus == LoginStatus.SUCCESS) {
    anomalous = loginAnomalyService.isAnomalousLogin(
            user.getId(),
            ipAddress,
            userAgent
    );
}

    securityEventRepository.save(event);

    // Device tracking is only relevant for successful logins
    if (user != null && loginStatus == LoginStatus.SUCCESS) {

        boolean newDevice =
                deviceService.recordLoginDevice(
                        user,
                        ipAddress,
                        userAgent
                );

        // Generate a separate security event for a new device
        if (newDevice) {

            SecurityEvent newDeviceEvent =
                    new SecurityEvent();

            newDeviceEvent.setUser(user);
            newDeviceEvent.setEventType(EventType.NEW_DEVICE);
            newDeviceEvent.setLoginStatus(LoginStatus.SUCCESS);
            newDeviceEvent.setIpAddress(ipAddress);
            newDeviceEvent.setUserAgent(userAgent);

            securityEventRepository.save(newDeviceEvent);
        }
    }
}

    // =========================
    // GET USER LOGIN HISTORY
    // =========================
    public List<SecurityEventResponse> getLoginHistory(Long userId) {

        List<SecurityEvent> events =
                securityEventRepository
                        .findByUserIdOrderByEventTimeDesc(userId);

        return events.stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================
    // CONVERT ENTITY TO DTO
    // =========================
    private SecurityEventResponse toResponse(
            SecurityEvent event
    ) {

        return new SecurityEventResponse(
                event.getId(),
                event.getEventType().name(),
                event.getLoginStatus(),
                event.getEventTime(),
                event.getIpAddress(),
                event.getUserAgent()
        );
    }
}