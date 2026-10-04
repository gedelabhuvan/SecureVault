package com.securevault.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/admin/users")
public class AdminRoleController {

    private final UserRepository userRepository;

    public AdminRoleController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<?> updateRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request,
            Authentication authentication
    ) {

        String newRole = request.get("role");

       if (newRole == null ||
        (!newRole.equals("USER")
        && !newRole.equals("TEAM_MEMBER")
        && !newRole.equals("ADMIN"))) {

    return ResponseEntity.badRequest()
            .body(Map.of(
                    "message",
                    "Role must be USER, TEAM_MEMBER or ADMIN."
            ));
}

        User currentAdmin = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Admin user not found"));

        // Prevent an admin from accidentally removing their own admin access.
        if (currentAdmin.getId().equals(userId)
                && !newRole.equals("ADMIN")) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "You cannot remove your own ADMIN role."
                    )
            );
        }

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        user.setRole(newRole);
        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of(
                        "message", "User role updated successfully.",
                        "userId", user.getId(),
                        "role", user.getRole()
                )
        );
    }
}