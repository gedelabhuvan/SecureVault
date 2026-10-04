package com.securevault.backend.controller;

import com.securevault.backend.dto.LoginResponse;
import com.securevault.backend.entity.User;
import com.securevault.backend.service.MfaChallengeService;
import com.securevault.backend.security.JwtService;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.service.MfaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth/mfa")
public class MfaController {

    private final MfaService mfaService;
    private final UserRepository userRepository;
    private final MfaChallengeService mfaChallengeService;
    private final JwtService jwtService;
    public MfaController(
            MfaService mfaService,
            UserRepository userRepository,
            MfaChallengeService mfaChallengeService,
            JwtService jwtService
    ) {
        this.mfaService = mfaService;
        this.userRepository = userRepository;
        this.mfaChallengeService = mfaChallengeService;
        this.jwtService = jwtService;
    }

    @PostMapping("/setup")
    public ResponseEntity<?> setupMfa(
            Authentication authentication
    ) {
        String email = authentication.getName();

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.isMfaEnabled()) {
            return ResponseEntity.badRequest()
                    .body("MFA is already enabled");
        }

        String secret = mfaService.generateSecret();

        user.setMfaSecret(secret);
        userRepository.save(user);

        return ResponseEntity.ok(
                new MfaSetupResponse(
                        "MFA secret generated",
                        secret
                )
        );
    }

    @PostMapping(value="/setup/verify")
public ResponseEntity<?> verifyMfaSetup(
        Authentication authentication,
        @RequestParam int code
) {
    String email = authentication.getName();

    User user = userRepository
            .findByEmail(email)
            .orElseThrow(() ->
                    new RuntimeException("User not found"));

    if (user.getMfaSecret() == null ||
            user.getMfaSecret().isBlank()) {

        return ResponseEntity.badRequest()
                .body("MFA setup has not been started");
    }

    boolean valid =
            mfaService.verifyCode(
                    user.getMfaSecret(),
                    code
            );

    if (!valid) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid MFA code");
    }

    user.setMfaEnabled(true);
    userRepository.save(user);

    return ResponseEntity.ok(
            "MFA enabled successfully"
    );
}

    @PostMapping("/verify")
public ResponseEntity<?> verifyMfa(
        @RequestParam String challengeId,
        @RequestParam int code
) {
    String email =
            mfaChallengeService.getEmail(challengeId);

    if (email == null) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("MFA challenge is invalid or expired");
    }

    User user = userRepository
            .findByEmail(email)
            .orElseThrow(() ->
                    new RuntimeException("User not found"));

    if (!user.isMfaEnabled()) {
        return ResponseEntity
                .badRequest()
                .body("MFA is not enabled");
    }

    boolean valid =
            mfaService.verifyCode(
                    user.getMfaSecret(),
                    code
            );

    if (!valid) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid MFA code");
    }

    mfaChallengeService.removeChallenge(challengeId);

    String token =
            jwtService.generateToken(user.getEmail());

    return ResponseEntity.ok(
            new LoginResponse(
                    "Login successful",
                    token,
                    "Bearer"
            )
    );
}

    public record MfaSetupResponse(
            String message,
            String secret
    ) {}
}