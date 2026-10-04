package com.securevault.backend.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.securevault.backend.dto.LoginRequest;
import com.securevault.backend.dto.LoginResponse;
import com.securevault.backend.dto.RegisterRequest;
import com.securevault.backend.dto.RegisterResponse;
import com.securevault.backend.entity.SecurityEvent.LoginStatus;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SecurityEventService securityEventService;
    private final MfaService mfaService;
    private final MfaChallengeService mfaChallengeService;
    private final UserSessionService userSessionService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            SecurityEventService securityEventService,
            MfaService mfaService,
            MfaChallengeService mfaChallengeService,
            UserSessionService userSessionService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.securityEventService = securityEventService;
        this.mfaService = mfaService;
        this.mfaChallengeService = mfaChallengeService;
        this.userSessionService = userSessionService;
    }

    // =========================
    // USER REGISTRATION
    // =========================
    public RegisterResponse register(RegisterRequest request) {

        // Check duplicate username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(
                    "Username already exists"
            );
        }

        // Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already exists"
            );
        }

        // Create new user
        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        // Hash password using BCrypt
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        // MFA will be implemented later
        user.setMfaEnabled(false);

        user.setRole("USER");

        // Save user to PostgreSQL
        User savedUser = userRepository.save(user);

        // Return safe response
        return new RegisterResponse(
                "User registered successfully",
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail()
        );
    }

    // =========================
    // USER LOGIN
    // =========================
    public LoginResponse login(
            LoginRequest request,
            String ipAddress,
            String userAgent
    ) {

        /*
         * Find the user first.
         *
         * This allows us to associate failed login attempts
         * with an existing account when the email is registered.
         */
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        try {

            // Authenticate email + password
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            if(user != null && user.isMfaEnabled()) {
                // MFA is enabled, generate a challenge
                String challengeId = mfaChallengeService.createChallenge(user.getEmail());

                // Return response indicating MFA is required
                return new LoginResponse(
                        "MFA verification required",
                        true,
                        challengeId
                );
            }


            // MFA is not enabled, proceed with normal login flow
            securityEventService.recordLoginEvent(
                    user,
                    LoginStatus.SUCCESS,
                    ipAddress,
                    userAgent
            );

            // Generate JWT after successful authentication
            String token = jwtService.generateToken(
                    request.getEmail()
            );

            userSessionService.createSession(
        user,
        token,
        ipAddress,
        userAgent
);

            // Return JWT
            return new LoginResponse(
                    "Login successful",
                    token,
                    "Bearer"
            );

        } catch (RuntimeException exception) {

            // Record failed login
            securityEventService.recordLoginEvent(
                    user,
                    LoginStatus.FAILURE,
                    ipAddress,
                    userAgent
            );

            // Preserve existing authentication behavior
            throw exception;
        }
    }
}