package com.securevault.backend.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(
            OAuth2UserRequest userRequest
    ) throws OAuth2AuthenticationException {

        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    "Google account did not provide an email address"
            );
        }

        User user = userRepository
                .findByEmail(email)
                .orElseGet(() -> createUser(email, name));

        return oauth2User;
    }

    private User createUser(String email, String name) {

        User user = new User();

        user.setEmail(email);

        String username =
                name != null && !name.isBlank()
                        ? name.replaceAll("\\s+", "").toLowerCase()
                        : email.substring(0, email.indexOf("@"));

        if (userRepository.existsByUsername(username)) {
            username = username + "_" + UUID.randomUUID()
                    .toString()
                    .substring(0, 8);
        }

        user.setUsername(username);

        // OAuth users authenticate through Google.
        // A random BCrypt password prevents use of a predictable password.
        user.setPasswordHash(
                passwordEncoder.encode(
                        UUID.randomUUID().toString()
                )
        );

        user.setMfaEnabled(false);

        return userRepository.save(user);
    }
}