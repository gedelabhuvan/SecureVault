package com.securevault.backend.controller;

import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test/protected")
    public Map<String, Object> protectedEndpoint(
            Authentication authentication
    ) {

        return Map.of(
                "message", "You accessed a protected endpoint!",
                "user", authentication.getName()
        );
    }
}
