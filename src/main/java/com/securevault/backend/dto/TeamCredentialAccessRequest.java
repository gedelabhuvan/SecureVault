package com.securevault.backend.dto;

import java.time.LocalDateTime;

import com.securevault.backend.entity.TeamCredentialAccess.PermissionLevel;

public class TeamCredentialAccessRequest {

    private Long userId;
    private PermissionLevel permissionLevel;
    private LocalDateTime expirationDate;

    public TeamCredentialAccessRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(PermissionLevel permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }
}