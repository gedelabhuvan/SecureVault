package com.securevault.backend.dto;

public class MfaChallengeResponse {

    private String message;
    private boolean mfaRequired;
    private String email;

    public MfaChallengeResponse(
            String message,
            boolean mfaRequired,
            String email
    ) {
        this.message = message;
        this.mfaRequired = mfaRequired;
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public boolean isMfaRequired() {
        return mfaRequired;
    }

    public String getEmail() {
        return email;
    }
}