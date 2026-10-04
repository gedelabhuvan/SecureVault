package com.securevault.backend.dto;

import com.securevault.backend.entity.SharedCredential;

import java.time.LocalDateTime;

public class ShareCredentialRequest {

    private String sharedUserEmail;

    private SharedCredential.PermissionLevel permissionLevel;

    private LocalDateTime expirationDate;

    public ShareCredentialRequest() {
    }

    public String getSharedUserEmail() {
        return sharedUserEmail;
    }

    public void setSharedUserEmail(String sharedUserEmail) {
        this.sharedUserEmail = sharedUserEmail;
    }

    public SharedCredential.PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(
            SharedCredential.PermissionLevel permissionLevel
    ) {
        this.permissionLevel = permissionLevel;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(
            LocalDateTime expirationDate
    ) {
        this.expirationDate = expirationDate;
    }
}
