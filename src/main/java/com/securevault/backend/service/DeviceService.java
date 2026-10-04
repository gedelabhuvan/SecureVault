package com.securevault.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;

import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

import com.securevault.backend.entity.Device;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.DeviceRepository;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public boolean recordLoginDevice(
            User user,
            String ipAddress,
            String userAgent
    ) {

        if (user == null) {
            return false;
        }

        String safeUserAgent =
                userAgent == null ? "UNKNOWN" : userAgent;

        String fingerprint =
                createFingerprint(safeUserAgent);

        var existingDevice =
                deviceRepository.findByUserIdAndDeviceFingerprint(
                        user.getId(),
                        fingerprint
                );

        if (existingDevice.isPresent()) {

            Device device = existingDevice.get();

            device.setIpAddress(ipAddress);
            device.setUserAgent(safeUserAgent);
            device.setLastSeen(LocalDateTime.now());
            device.setLoginCount(
                    device.getLoginCount() + 1
            );

            deviceRepository.save(device);

            return false;
        }

        Device device = new Device();

        device.setUser(user);
        device.setDeviceFingerprint(fingerprint);
        device.setDeviceName(detectDevice(safeUserAgent));
        device.setBrowser(detectBrowser(safeUserAgent));
        device.setOperatingSystem(
                detectOperatingSystem(safeUserAgent)
        );
        device.setIpAddress(ipAddress);
        device.setUserAgent(safeUserAgent);
        device.setFirstSeen(LocalDateTime.now());
        device.setLastSeen(LocalDateTime.now());
        device.setLoginCount(1);
        device.setTrusted(false);

        deviceRepository.save(device);

        return true;
    }

    public List<Device> getUserDevices(Long userId) {

        return deviceRepository
                .findByUserIdOrderByLastSeenDesc(userId);
    }

    @Transactional
public boolean setTrusted(
        Long deviceId,
        Long userId,
        boolean trusted
) {
    return deviceRepository
            .findByIdAndUserId(deviceId, userId)
            .map(device -> {
                device.setTrusted(trusted);
                deviceRepository.save(device);
                return true;
            })
            .orElse(false);
}

@Transactional
public boolean removeDevice(
        Long deviceId,
        Long userId
) {
    Optional<Device> device =
            deviceRepository.findByIdAndUserId(
                    deviceId,
                    userId
            );

    if (device.isEmpty()) {
        return false;
    }

    deviceRepository.delete(device.get());
    return true;
}

    private String createFingerprint(String userAgent) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            userAgent.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder result =
                    new StringBuilder();

            for (byte b : hash) {
                result.append(
                        String.format("%02x", b)
                );
            }

            return result.toString();

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "Unable to create device fingerprint",
                    exception
            );
        }
    }

    private String detectDevice(String userAgent) {

        if (userAgent.contains("Android")) {
            return "Android Device";
        }

        if (userAgent.contains("iPhone")) {
            return "iPhone";
        }

        if (userAgent.contains("iPad")) {
            return "iPad";
        }

        if (userAgent.contains("Windows")) {
            return "Windows PC";
        }

        if (userAgent.contains("Macintosh")) {
            return "Mac";
        }

        if (userAgent.contains("Linux")) {
            return "Linux Device";
        }

        return "Unknown Device";
    }

    private String detectBrowser(String userAgent) {

        if (userAgent.contains("Edg/")) {
            return "Microsoft Edge";
        }

        if (userAgent.contains("Chrome/")) {
            return "Google Chrome";
        }

        if (userAgent.contains("Firefox/")) {
            return "Mozilla Firefox";
        }

        if (userAgent.contains("OPR/")) {
            return "Opera";
        }

        if (userAgent.contains("Safari/")
                && !userAgent.contains("Chrome/")) {
            return "Safari";
        }

        return "Unknown Browser";
    }

    private String detectOperatingSystem(String userAgent) {

        if (userAgent.contains("Windows")) {
            return "Windows";
        }

        if (userAgent.contains("Android")) {
            return "Android";
        }

        if (userAgent.contains("iPhone")
                || userAgent.contains("iPad")) {
            return "iOS";
        }

        if (userAgent.contains("Mac OS")
                || userAgent.contains("Macintosh")) {
            return "macOS";
        }

        if (userAgent.contains("Linux")) {
            return "Linux";
        }

        return "Unknown OS";
    }
}