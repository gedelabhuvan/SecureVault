package com.securevault.backend.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.securevault.backend.entity.PasswordResetToken;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.PasswordResetTokenRepository;
import com.securevault.backend.repository.UserRepository;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            PasswordResetTokenRepository passwordResetTokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.passwordResetTokenRepository =
                passwordResetTokenRepository;

        this.userRepository =
                userRepository;

        this.passwordEncoder =
                passwordEncoder;
    }

    /**
     * Creates a password reset token for the supplied email.
     *
     * Development version:
     * The token is returned to the frontend so the reset
     * workflow can be tested without an email service.
     */
    @Transactional
    public String createResetToken(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No account found with this email address."
                        )
                );

        // Remove older reset tokens for this user.
        passwordResetTokenRepository
                .deleteByUserId(user.getId());

        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        PasswordResetToken resetToken =
                new PasswordResetToken();

        resetToken.setToken(token);
        resetToken.setUser(user);

        // Token valid for 15 minutes.
        resetToken.setExpirationDate(
                LocalDateTime.now().plusMinutes(15)
        );

        resetToken.setUsed(false);

        passwordResetTokenRepository.save(resetToken);

        return token;
    }


    /**
     * Resets the user's password using a valid reset token.
     */
    @Transactional
    public void resetPassword(
            String token,
            String newPassword
    ) {

        if (token == null || token.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reset token is required."
            );
        }

        if (newPassword == null || newPassword.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password is required."
            );
        }

        if (newPassword.length() < 8) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must contain at least 8 characters."
            );
        }

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByToken(token)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Invalid reset token."
                                )
                        );

        if (resetToken.isUsed()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This reset token has already been used."
            );
        }

        if (resetToken
                .getExpirationDate()
                .isBefore(LocalDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This reset token has expired."
            );
        }

        User user = resetToken.getUser();

        /*
         * IMPORTANT:
         * User login passwords are stored using BCrypt,
         * not AES encryption.
         */
        String hashedPassword =
                passwordEncoder.encode(newPassword);

        user.setPasswordHash(hashedPassword);

        userRepository.save(user);

        resetToken.setUsed(true);

        passwordResetTokenRepository.save(resetToken);
    }
}