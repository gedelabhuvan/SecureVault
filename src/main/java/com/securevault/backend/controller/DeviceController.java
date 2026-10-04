package com.securevault.backend.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.securevault.backend.dto.DeviceResponse;
import com.securevault.backend.entity.Device;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.service.DeviceService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/security/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final UserRepository userRepository;

    public DeviceController(
            DeviceService deviceService,
            UserRepository userRepository) {

        this.deviceService = deviceService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<DeviceResponse> getMyDevices(
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        List<Device> devices =
                deviceService.getUserDevices(user.getId());

        return devices.stream()
                .map(DeviceResponse::new)
                .toList();
    }
    @PutMapping("/{deviceId}/trust")
public ResponseEntity<?> trustDevice(
        @PathVariable Long deviceId,
        Authentication authentication) {

    User user = userRepository
            .findByEmail(authentication.getName())
            .orElseThrow(() ->
                    new RuntimeException("User not found"));

    boolean updated = deviceService.setTrusted(
            deviceId,
            user.getId(),
            true
    );

    if (!updated) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(
            java.util.Map.of(
                    "message",
                    "Device trusted successfully."
            )
    );
}

@PutMapping("/{deviceId}/untrust")
public ResponseEntity<?> untrustDevice(
        @PathVariable Long deviceId,
        Authentication authentication) {

    User user = userRepository
            .findByEmail(authentication.getName())
            .orElseThrow(() ->
                    new RuntimeException("User not found"));

    boolean updated = deviceService.setTrusted(
            deviceId,
            user.getId(),
            false
    );

    if (!updated) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(
            java.util.Map.of(
                    "message",
                    "Device untrusted successfully."
            )
    );
}

@DeleteMapping("/{deviceId}")
public ResponseEntity<?> removeDevice(
        @PathVariable Long deviceId,
        Authentication authentication) {

    User user = userRepository
            .findByEmail(authentication.getName())
            .orElseThrow(() ->
                    new RuntimeException("User not found"));

    boolean removed = deviceService.removeDevice(
            deviceId,
            user.getId()
    );

    if (!removed) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(
            java.util.Map.of(
                    "message",
                    "Device removed successfully."
            )
    );
}
}