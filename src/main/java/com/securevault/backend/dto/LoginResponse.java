package com.securevault.backend.dto;

public class LoginResponse {

    private String message;
    private String token;
    private String type;
    private boolean mfaRequired;
    private String challengeId;

    public LoginResponse(
            String message,
            String token,
            String type
    ) {
        this.message = message;
        this.token = token;
        this.type = type;
        this.mfaRequired = false;
    }

    public LoginResponse(
            String message,
            boolean mfaRequired,
            String challengeId
    ) {
        this.message = message;
        this.token = null;
        this.type = null;
        this.mfaRequired = mfaRequired;
        this.challengeId = challengeId;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getTokenType() {
    return type;
}

    public boolean isMfaRequired() {
        return mfaRequired;
    }

    public String getChallengeId() {
        return challengeId;
    }
}