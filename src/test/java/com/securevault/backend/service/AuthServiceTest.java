package com.securevault.backend.service;

import com.securevault.backend.dto.LoginRequest;
import com.securevault.backend.dto.LoginResponse;
import com.securevault.backend.dto.RegisterRequest;
import com.securevault.backend.dto.RegisterResponse;
import com.securevault.backend.entity.SecurityEvent.LoginStatus;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.security.JwtService;
import com.securevault.backend.service.UserSessionService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
        @Mock 
        private UserSessionService userSessionService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private SecurityEventService securityEventService;

    @InjectMocks
    private AuthService authService;


    // =========================================================
    // REGISTRATION TESTS
    // =========================================================

    @Test
    void register_shouldRegisterUserSuccessfully() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@gmail.com");
        request.setPassword("password123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@gmail.com");
        savedUser.setPasswordHash("encodedPassword");

        when(userRepository.existsByUsername("testuser"))
                .thenReturn(false);

        when(userRepository.existsByEmail("test@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        RegisterResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("User registered successfully", response.getMessage());
        assertEquals(1L, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@gmail.com", response.getEmail());

        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }


    @Test
    void register_shouldRejectDuplicateUsername() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setEmail("new@gmail.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("existinguser"))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                "Username already exists",
                exception.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }


    @Test
    void register_shouldRejectDuplicateEmail() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("existing@gmail.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("newuser"))
                .thenReturn(false);

        when(userRepository.existsByEmail("existing@gmail.com"))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                "Email already exists",
                exception.getMessage()
        );

        verify(userRepository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }


    @Test
    void register_shouldEncodePasswordBeforeSaving() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("secureuser");
        request.setEmail("secure@gmail.com");
        request.setPassword("mypassword");

        when(userRepository.existsByUsername("secureuser"))
                .thenReturn(false);

        when(userRepository.existsByEmail("secure@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("mypassword"))
                .thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(2L);
        savedUser.setUsername("secureuser");
        savedUser.setEmail("secure@gmail.com");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        authService.register(request);

        verify(passwordEncoder)
                .encode("mypassword");

        verify(userRepository).save(
                argThat(user ->
                        "encodedPassword".equals(
                                user.getPasswordHash()
                        )
                )
        );
    }


    @Test
    void register_shouldDisableMfaByDefault() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("mfauser");
        request.setEmail("mfa@gmail.com");
        request.setPassword("password123");

        when(userRepository.existsByUsername("mfauser"))
                .thenReturn(false);

        when(userRepository.existsByEmail("mfa@gmail.com"))
                .thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(3L);
        savedUser.setUsername("mfauser");
        savedUser.setEmail("mfa@gmail.com");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        authService.register(request);

        verify(userRepository).save(
                argThat(user -> !user.isMfaEnabled())
        );
    }


    // =========================================================
    // LOGIN TESTS
    // =========================================================

    @Test
    void login_shouldReturnJwtOnSuccessfulAuthentication() {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(authenticationManager.authenticate(any()))
                .thenReturn(mock(Authentication.class));

        when(jwtService.generateToken("test@gmail.com"))
                .thenReturn("jwt-token");

        LoginResponse response =
                authService.login(
                        request,
                        "127.0.0.1",
                        "JUnit-Test"
                );

        assertNotNull(response);
        assertEquals("Login successful", response.getMessage());
        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());

        verify(jwtService)
                .generateToken("test@gmail.com");

        verify(securityEventService)
                .recordLoginEvent(
                        user,
                        LoginStatus.SUCCESS,
                        "127.0.0.1",
                        "JUnit-Test"
                );
    }


    @Test
    void login_shouldRecordFailureWhenAuthenticationFails() {

        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        RuntimeException authenticationException =
                new RuntimeException("Authentication failed");

        when(authenticationManager.authenticate(any()))
                .thenThrow(authenticationException);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> authService.login(
                                request,
                                "127.0.0.1",
                                "JUnit-Test"
                        )
                );

        assertEquals(
                "Authentication failed",
                exception.getMessage()
        );

        verify(securityEventService)
                .recordLoginEvent(
                        user,
                        LoginStatus.FAILURE,
                        "127.0.0.1",
                        "JUnit-Test"
                );

        verify(jwtService, never())
                .generateToken(anyString());
    }


    @Test
    void login_shouldRecordFailureWhenUserDoesNotExist() {

        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@gmail.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        RuntimeException authenticationException =
                new RuntimeException("Authentication failed");

        when(authenticationManager.authenticate(any()))
                .thenThrow(authenticationException);

        assertThrows(
                RuntimeException.class,
                () -> authService.login(
                        request,
                        "127.0.0.1",
                        "JUnit-Test"
                )
        );

        verify(securityEventService)
                .recordLoginEvent(
                        isNull(),
                        eq(LoginStatus.FAILURE),
                        eq("127.0.0.1"),
                        eq("JUnit-Test")
                );

        verify(jwtService, never())
                .generateToken(anyString());
    }
}