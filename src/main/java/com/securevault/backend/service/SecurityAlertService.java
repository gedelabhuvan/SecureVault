package com.securevault.backend.service;

import com.securevault.backend.entity.SecurityAlert;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.SecurityAlertRepository;
import org.springframework.stereotype.Service;

@Service
public class SecurityAlertService {

    private final SecurityAlertRepository securityAlertRepository;

    public SecurityAlertService(
            SecurityAlertRepository securityAlertRepository) {
        this.securityAlertRepository = securityAlertRepository;
    }

    public SecurityAlert createAlert(
            User user,
            String alertType,
            String message) {

        SecurityAlert alert = new SecurityAlert();

        alert.setUser(user);
        alert.setAlertType(alertType);
        alert.setMessage(message);
        alert.setStatus(SecurityAlert.AlertStatus.ACTIVE);

        return securityAlertRepository.save(alert);
    }

    public void resolveAlert(Long alertId) {

        SecurityAlert alert =
                securityAlertRepository.findById(alertId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Security alert not found"));

        alert.setStatus(SecurityAlert.AlertStatus.RESOLVED);

        securityAlertRepository.save(alert);
    }
}
