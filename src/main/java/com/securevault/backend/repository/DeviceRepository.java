package com.securevault.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securevault.backend.entity.Device;

public interface DeviceRepository
        extends JpaRepository<Device, Long> {

    Optional<Device> findByUserIdAndDeviceFingerprint(
            Long userId,
            String deviceFingerprint
    );

    List<Device> findByUserIdOrderByLastSeenDesc(
            Long userId
    );
    Optional<Device> findByIdAndUserId(
        Long id,
        Long userId
);

void deleteByIdAndUserId(
        Long id,
        Long userId
);
}
