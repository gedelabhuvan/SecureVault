package com.securevault.backend.service;

import com.securevault.backend.entity.User;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Service;

@Service
public class MfaService {

    private final GoogleAuthenticator googleAuthenticator;

    public MfaService() {
        this.googleAuthenticator = new GoogleAuthenticator();
    }

    public String generateSecret() {
        GoogleAuthenticatorKey key =
                googleAuthenticator.createCredentials();

        return key.getKey();
    }

    public boolean verifyCode(String secret, int code) {
        if (secret == null || secret.isBlank()) {
            return false;
        }

        return googleAuthenticator.authorize(secret, code);
    }

    public void enableMfa(User user, String secret) {
        user.setMfaSecret(secret);
        user.setMfaEnabled(true);
    }

    public void disableMfa(User user) {
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
    }
}