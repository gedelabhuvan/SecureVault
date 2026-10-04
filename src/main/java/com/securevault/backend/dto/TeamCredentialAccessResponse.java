package com.securevault.backend.dto;

import java.time.LocalDateTime;

import com.securevault.backend.entity.TeamCredentialAccess.PermissionLevel;

public class TeamCredentialAccessResponse {

    private Long accessId;
    private Long teamId;
    private String teamName;

    private Long credentialId;
    private String title;
    private String username;
    private String password;

    private Long userId;
    private String usernameOfMember;
    private String email;

    private PermissionLevel permissionLevel;

    private LocalDateTime expirationDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public TeamCredentialAccessResponse(
            Long accessId,
            Long teamId,
            String teamName,
            Long credentialId,
            String title,
            String username,
            String password,
            Long userId,
            String usernameOfMember,
            String email,
            PermissionLevel permissionLevel,
            LocalDateTime expirationDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.accessId = accessId;
        this.teamId = teamId;
        this.teamName = teamName;
        this.credentialId = credentialId;
        this.title = title;
        this.username = username;
        this.password = password;
        this.userId = userId;
        this.usernameOfMember = usernameOfMember;
        this.email = email;
        this.permissionLevel = permissionLevel;
        this.expirationDate = expirationDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public Long getAccessId() {
        return accessId;
    }


    public Long getTeamId() {
        return teamId;
    }


    public String getTeamName() {
        return teamName;
    }


    public Long getCredentialId() {
        return credentialId;
    }


    public String getTitle() {
        return title;
    }


    public String getUsername() {
        return username;
    }


    public String getPassword() {
        return password;
    }


    public Long getUserId() {
        return userId;
    }


    public String getUsernameOfMember() {
        return usernameOfMember;
    }


    public String getEmail() {
        return email;
    }


    public PermissionLevel getPermissionLevel() {
        return permissionLevel;
    }


    // ==========================================
    // EXPIRATION DATE
    // ==========================================

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}