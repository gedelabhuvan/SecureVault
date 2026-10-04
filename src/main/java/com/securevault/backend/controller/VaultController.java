package com.securevault.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

import com.securevault.backend.dto.CredentialRequest;
import com.securevault.backend.dto.CredentialResponse;
import com.securevault.backend.service.VaultService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vault")
public class VaultController {

    private final VaultService vaultService;

    public VaultController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    // Add credential
    @PostMapping("/credentials")
    public ResponseEntity<CredentialResponse> addCredential(
            @Valid @RequestBody CredentialRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        CredentialResponse response =
                vaultService.addCredential(request, email);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get logged-in user's credentials
    @GetMapping("/credentials")
    public ResponseEntity<List<CredentialResponse>> getCredentials(
            Authentication authentication
    ) {

        String email = authentication.getName();

        List<CredentialResponse> credentials =
                vaultService.getCredentials(email);

        return ResponseEntity.ok(credentials);
    }

    @GetMapping("/credentials/search")
public ResponseEntity<List<CredentialResponse>> searchCredentials(
        @RequestParam String title,
        Authentication authentication
) {

    String email = authentication.getName();

    List<CredentialResponse> credentials =
            vaultService.searchCredentials(
                    title,
                    email
            );

    return ResponseEntity.ok(credentials);
}

@GetMapping("/credentials/favorites")
public ResponseEntity<List<CredentialResponse>> getFavoriteCredentials(
        Authentication authentication
) {
    String email = authentication.getName();

    List<CredentialResponse> credentials =
            vaultService.getFavoriteCredentials(email);

    return ResponseEntity.ok(credentials);
}

@GetMapping("/credentials/favorites-first")
public ResponseEntity<List<CredentialResponse>> getCredentialsWithFavoritesFirst(
        Authentication authentication
) {
    String email = authentication.getName();

    List<CredentialResponse> credentials =
            vaultService.getCredentialsWithFavoritesFirst(email);

    return ResponseEntity.ok(credentials);
}

@GetMapping("/credentials/generate-password")
public ResponseEntity<?> generatePassword(
        @RequestParam(defaultValue = "16") int length
) {
    try {
        String password = vaultService.generatePassword(length);

        return ResponseEntity.ok(
                Map.of(
                        "password", password,
                        "length", password.length()
                )
        );
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
    }
}

@PutMapping("/credentials/{id}/favorite")
public ResponseEntity<?> setFavorite(
        @PathVariable Long id,
        @RequestParam boolean favorite,
        Authentication authentication
) {
    String email = authentication.getName();

    vaultService.setFavorite(id, favorite, email);

    return ResponseEntity.ok(
            Map.of(
                    "message", favorite
                            ? "Credential added to favorites."
                            : "Credential removed from favorites.",
                    "favorite", favorite
            )
    );
}

@GetMapping("/credentials/sort")
public ResponseEntity<List<CredentialResponse>> getSortedCredentials(
        @RequestParam String sortBy,
        Authentication authentication
) {

    String email = authentication.getName();

    List<CredentialResponse> credentials =
            vaultService.getSortedCredentials(
                    sortBy,
                    email
            );

    return ResponseEntity.ok(credentials);
}

    // Update credential
    @PutMapping("/credentials/{id}")
    public ResponseEntity<CredentialResponse> updateCredential(
            @PathVariable Long id,
            @Valid @RequestBody CredentialRequest request,
            Authentication authentication
    ) {

        String email = authentication.getName();

        CredentialResponse response =
                vaultService.updateCredential(
                        id,
                        request,
                        email
                );

        return ResponseEntity.ok(response);
    }

    // Delete credential
    @DeleteMapping("/credentials/{id}")
    public ResponseEntity<Void> deleteCredential(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email = authentication.getName();

        vaultService.deleteCredential(id, email);

        return ResponseEntity.noContent().build();
    }
}