package com.securevault.backend.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.security.JwtService;
import com.securevault.backend.service.UserSessionService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2AuthenticationSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserSessionService userSessionService;
    private final UserRepository userRepository;

    public OAuth2AuthenticationSuccessHandler(
            JwtService jwtService,
            UserSessionService userSessionService,
            UserRepository userRepository
    ) {
        this.jwtService = jwtService;
        this.userSessionService = userSessionService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauth2User =
                (OAuth2User) authentication.getPrincipal();

        String email =
                oauth2User.getAttribute("email");

        if (email == null || email.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Google account email not available"
            );
            return;
        }

        User user = userRepository
        .findByEmail(email)
        .orElseThrow();
        
        String token = jwtService.generateToken(email);

userSessionService.createSession(
        user,
        token,
        request.getRemoteAddr(),
        request.getHeader("User-Agent")
);


        String redirectUrl =
                "http://localhost:5173/oauth2/success?token="
                        + java.net.URLEncoder.encode(
                                token,
                                java.nio.charset.StandardCharsets.UTF_8
                        );

        getRedirectStrategy().sendRedirect(
                request,
                response,
                redirectUrl
        );
    }
}