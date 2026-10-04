package com.securevault.backend.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.securevault.backend.service.CustomOAuth2UserService;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final CustomOAuth2UserService customOAuth2UserService;
private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService userDetailsService,
            AuthenticationEntryPointImpl authenticationEntryPoint,
            CustomOAuth2UserService customOAuth2UserService,
            OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
    }

  @Bean
public AuthenticationProvider authenticationProvider(
        PasswordEncoder passwordEncoder
) {

    DaoAuthenticationProvider provider =
            new DaoAuthenticationProvider(userDetailsService);

    provider.setPasswordEncoder(passwordEncoder);

    return provider;
}
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, PasswordEncoder passwordEncoder
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(
                        corsConfigurationSource()
                ))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.IF_REQUIRED
                        )
                )

                .authenticationProvider(authenticationProvider(passwordEncoder))

                .authorizeHttpRequests(auth -> auth

                        /*
                         * =========================================
                         * PUBLIC AUTHENTICATION ENDPOINTS
                         * =========================================
                         *
                         * These endpoints do NOT require JWT.
                         */
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/mfa/verify",
                                "/error"
                        ).permitAll()

                        /*
                         * =========================================
                         * CORS PREFLIGHT
                         * =========================================
                         */
                        .requestMatchers("/oauth2/**","/login/oauth2/**").permitAll()

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        /*
                         * =========================================
                         * ERROR
                         * =========================================
                         */
                        .requestMatchers(
                                "/error"
                        ).permitAll()

                        /*
                         * =========================================
                         * TEST PROTECTED ENDPOINT
                         * =========================================
                         */
                        .requestMatchers(
                                "/api/test/protected"
                        ).authenticated()

                        /*
                         * =========================================
                         * PERSONAL VAULT
                         * =========================================
                         */
                        .requestMatchers(
                                "/api/vault/**"
                        ).authenticated()
                        
                        .requestMatchers("/api/team/**")
.hasAnyAuthority("TEAM_MEMBER", "ADMIN")

                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                        /*
                         * =========================================
                         * ALL OTHER ENDPOINTS
                         * =========================================
                         */
                        .anyRequest().authenticated()
                )

               .oauth2Login(oauth2 -> oauth2
    .userInfoEndpoint(userInfo ->
        userInfo.userService(customOAuth2UserService)
    )
    .successHandler(oAuth2AuthenticationSuccessHandler)
    .failureHandler((request, response, exception) -> {
        response.sendRedirect(
            "http://localhost:5173/login?oauth2Error=true"
        );
    })
)

                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                authenticationEntryPoint
                        )
                )

        
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}