package com.securevault.backend.dto;

import com.securevault.backend.entity.SharedCredential;

import java.time.LocalDateTime;

public class SharedCredentialResponse {

    private Long shareId;
    private Long credentialId;

    private String title;
    private String username;
    private String password;

    private Long ownerId;
    private String ownerUsername;

    private Long sharedUserId;
    private String sharedUserUsername;

    private SharedCredential.PermissionLevel permissionLevel;
    private SharedCredential.SharingStatus sharingStatus;

    private LocalDateTime expirationDate;

    public SharedCredentialResponse() {
    }

    public Long getShareId() {
        return shareId;
    }

    public void setShareId(Long shareId) {
        this.shareId = shareId;
    }

    public Long getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(Long credentialId) {
        this.credentialId = credentialId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public Long getSharedUserId() {
        return sharedUserId;
    }

    public void setSharedUserId(Long sharedUserId) {
        this.sharedUserId = sharedUserId;
    }

    public String getSharedUserUsername() {
        return sharedUserUsername;
    }

    public void setSharedUserUsername(String sharedUserUsername) {
        this.sharedUserUsername = sharedUserUsername;
    }

    public SharedCredential.PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(
            SharedCredential.PermissionLevel permissionLevel
    ) {
        this.permissionLevel = permissionLevel;
    }

    public SharedCredential.SharingStatus getSharingStatus() {
        return sharingStatus;
    }

    public void setSharingStatus(
            SharedCredential.SharingStatus sharingStatus
    ) {
        this.sharingStatus = sharingStatus;
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