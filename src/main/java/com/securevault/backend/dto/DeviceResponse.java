package com.securevault.backend.dto;

import java.time.LocalDateTime;

import com.securevault.backend.entity.Device;

public class DeviceResponse {

    private Long id;
    private String deviceFingerprint;
    private String deviceName;
    private String browser;
    private String operatingSystem;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime firstSeen;
    private LocalDateTime lastSeen;
    private long loginCount;
    private boolean trusted;

    public DeviceResponse(Device device) {
        this.id = device.getId();
        this.deviceFingerprint = device.getDeviceFingerprint();
        this.deviceName = device.getDeviceName();
        this.browser = device.getBrowser();
        this.operatingSystem = device.getOperatingSystem();
        this.ipAddress = device.getIpAddress();
        this.userAgent = device.getUserAgent();
        this.firstSeen = device.getFirstSeen();
        this.lastSeen = device.getLastSeen();
        this.loginCount = device.getLoginCount();
        this.trusted = device.isTrusted();
    }

    public Long getId() {
        return id;
    }

    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getBrowser() {
        return browser;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public LocalDateTime getFirstSeen() {
        return firstSeen;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public long getLoginCount() {
        return loginCount;
    }

    public boolean isTrusted() {
        return trusted;
    }
}
