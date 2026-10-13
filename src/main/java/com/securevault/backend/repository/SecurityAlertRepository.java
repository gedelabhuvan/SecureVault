package com.securevault.backend.repository;

import com.securevault.backend.entity.SecurityAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SecurityAlertRepository
        extends JpaRepository<SecurityAlert, Long> {

    long countByUserIdAndStatus(
            Long userId,
            SecurityAlert.AlertStatus status
    );
    List<SecurityAlert> findByUserId(
            Long userId
            
            
    );
}