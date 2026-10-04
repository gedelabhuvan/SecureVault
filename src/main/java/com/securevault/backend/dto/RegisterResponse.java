package com.securevault.backend.dto;

public class RegisterResponse {

    private String message;
    private Long userId;
    private String username;
    private String email;

    public RegisterResponse(
            String message,
            Long userId,
            String username,
            String email
    ) {
        this.message = message;
        this.userId = userId;
        this.username = username;
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}