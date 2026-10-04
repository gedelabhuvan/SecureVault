package com.securevault.backend.repository;

import com.securevault.backend.entity.SecurityAlert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityAlertRepository
        extends JpaRepository<SecurityAlert, Long> {

    long countByUserIdAndStatus(
            Long userId,
            SecurityAlert.AlertStatus status
    );
}