package com.securevault.backend.controller;

import com.securevault.backend.dto.CredentialResponse;
import com.securevault.backend.service.VaultService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final VaultService vaultService;

    public ReportController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @GetMapping("/credentials")
    public ResponseEntity<List<CredentialResponse>> getCredentialReport(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                vaultService.getCredentials(email)
        );
    }
    @GetMapping("/credentials/export")
public ResponseEntity<byte[]> exportCredentials(
        Authentication authentication) {

    String email = authentication.getName();

    List<CredentialResponse> credentials =
            vaultService.getCredentials(email);

    StringBuilder csv = new StringBuilder();

    csv.append("ID,Title,Username,Favorite\n");

    for (CredentialResponse credential : credentials) {
        csv.append(credential.getId()).append(",")
           .append("\"").append(credential.getTitle().replace("\"", "\"\"")).append("\",")
           .append("\"").append(credential.getUsername().replace("\"", "\"\"")).append("\",")
           .append(credential.isFavorite())
           .append("\n");
    }

    byte[] file = csv.toString()
            .getBytes(StandardCharsets.UTF_8);

    return ResponseEntity.ok()
            .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=securevault-credentials.csv"
            )
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(file);
}
}