package com.securevault.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(Authentication authentication) {

        List<Map<String, Object>> users =
                userRepository.findAll()
                        .stream()
                        .map(user -> Map.<String, Object>of(
                                "id", user.getId(),
                                "username", user.getUsername(),
                                "email", user.getEmail(),
                                "role", user.getRole()
                        ))
                        .toList();

        return ResponseEntity.ok(users);
    }
}