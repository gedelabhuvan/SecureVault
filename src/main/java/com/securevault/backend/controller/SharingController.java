package com.securevault.backend.controller;

import com.securevault.backend.dto.ShareCredentialRequest;
import com.securevault.backend.dto.SharedCredentialResponse;
import com.securevault.backend.entity.Credential;
import com.securevault.backend.entity.SharedCredential;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.security.JwtService;
import com.securevault.backend.service.SharingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sharing")
@CrossOrigin(
        origins = {
                "http://localhost:5173",
                "http://localhost:5174"
        }
)
public class SharingController {

    private final SharingService sharingService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public SharingController(
            SharingService sharingService,
            JwtService jwtService,
            UserRepository userRepository
    ) {
        this.sharingService = sharingService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    // =========================================================
    // 1. SHARE CREDENTIAL - OWNER
    // =========================================================
    //
    // POST /api/sharing/credentials/{credentialId}
    //
    // Example request:
    //
    // {
    //   "sharedUserEmail": "userb@gmail.com",
    //   "permissionLevel": "VIEW_ONLY",
    //   "expirationDate": "2026-12-31T23:59:59"
    // }
    //
    // =========================================================

    @PostMapping("/credentials/{credentialId}")
    public ResponseEntity<SharedCredentialResponse> shareCredential(
            @PathVariable Long credentialId,
            @RequestBody ShareCredentialRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {

        Long ownerId = getUserIdFromToken(
                authorizationHeader
        );

        SharedCredential sharedCredential =
                sharingService.shareCredential(
                        credentialId,
                        ownerId,
                        request.getSharedUserEmail(),
                        request.getPermissionLevel(),
                        request.getExpirationDate()
                );

        return ResponseEntity.ok(
                convertToResponse(sharedCredential)
        );
    }

    // =========================================================
    // 2. SHARED WITH ME
    // =========================================================
    //
    // GET /api/sharing/shared-with-me
    //
    // =========================================================

    @GetMapping("/shared-with-me")
    public ResponseEntity<List<SharedCredentialResponse>>
    getSharedWithMe(
            @RequestHeader("Authorization") String authorizationHeader
    ) {

        Long userId = getUserIdFromToken(
                authorizationHeader
        );

        List<SharedCredentialResponse> response =
                sharingService
                        .getSharedWithMe(userId)
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // 3. VIEW SHARED CREDENTIAL
    // =========================================================
    //
    // GET /api/sharing/credentials/{credentialId}
    //
    // =========================================================

    @GetMapping("/credentials/{credentialId}")
    public ResponseEntity<SharedCredentialResponse>
    getSharedCredential(
            @PathVariable Long credentialId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {

        Long userId = getUserIdFromToken(
                authorizationHeader
        );

        SharedCredential sharedCredential =
                sharingService.getSharedCredential(
                        credentialId,
                        userId
                );

        return ResponseEntity.ok(
                convertToResponse(sharedCredential)
        );
    }

    // =========================================================
    // 4. VIEW CREDENTIAL
    // =========================================================
    //
    // GET /api/sharing/credentials/{credentialId}/view
    //
    // =========================================================

    @GetMapping("/credentials/{credentialId}/view")
    public ResponseEntity<SharedCredentialResponse>
    viewCredential(
            @PathVariable Long credentialId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {

        Long userId = getUserIdFromToken(
                authorizationHeader
        );

        Credential credential =
                sharingService.getCredentialForViewing(
                        credentialId,
                        userId
                );

        SharedCredentialResponse response =
                new SharedCredentialResponse();

        response.setCredentialId(
                credential.getId()
        );

        response.setTitle(
                credential.getTitle()
        );

        response.setUsername(
                credential.getUsername()
        );

        response.setPassword(
                credential.getPassword()
        );

        if (credential.getUser() != null) {

            response.setOwnerId(
                    credential.getUser().getId()
            );

            response.setOwnerUsername(
                    credential.getUser().getUsername()
            );
        }

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // 5. EDIT SHARED CREDENTIAL
    // =========================================================
    //
    // PUT /api/sharing/credentials/{credentialId}
    //
    // VIEW_ONLY:
    //      blocked
    //
    // EDIT_ACCESS:
    //      allowed
    //
    // FULL_MANAGEMENT:
    //      allowed
    //
    // OWNER:
    //      allowed
    //
    // =========================================================

    @PutMapping("/credentials/{credentialId}")
    public ResponseEntity<SharedCredentialResponse>
    updateSharedCredential(
            @PathVariable Long credentialId,
            @RequestBody UpdateCredentialRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {

        Long userId = getUserIdFromToken(
                authorizationHeader
        );

        Credential credential =
                sharingService.updateSharedCredential(
                        credentialId,
                        userId,
                        request.getTitle(),
                        request.getUsername(),
                        request.getPassword()
                );

        SharedCredentialResponse response =
                new SharedCredentialResponse();

        response.setCredentialId(
                credential.getId()
        );

        response.setTitle(
                credential.getTitle()
        );

        response.setUsername(
                credential.getUsername()
        );

        response.setPassword(
                credential.getPassword()
        );

        if (credential.getUser() != null) {

            response.setOwnerId(
                    credential.getUser().getId()
            );

            response.setOwnerUsername(
                    credential.getUser().getUsername()
            );
        }

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // 6. GET ALL SHARES FOR A CREDENTIAL
    // =========================================================
    //
    // Owner only
    //
    // GET /api/sharing/credentials/{credentialId}/shares
    //
    // =========================================================

    @GetMapping("/credentials/{credentialId}/shares")
    public ResponseEntity<List<SharedCredentialResponse>>
    getCredentialShares(
            @PathVariable Long credentialId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {

        Long ownerId = getUserIdFromToken(
                authorizationHeader
        );

        List<SharedCredentialResponse> response =
                sharingService
                        .getCredentialShares(
                                credentialId,
                                ownerId
                        )
                        .stream()
                        .map(this::convertToResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    // =========================================================
    // 7. FULL MANAGEMENT - SHARE CREDENTIAL
    // =========================================================
    //
    // Owner OR FULL_MANAGEMENT user
    //
    // POST /api/sharing/credentials/{credentialId}/manage
    //
    // =========================================================

    @PostMapping("/credentials/{credentialId}/manage")
    public ResponseEntity<SharedCredentialResponse>
    shareCredentialAsManager(
            @PathVariable Long credentialId,
            @RequestBody ShareCredentialRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {

        Long managerId = getUserIdFromToken(
                authorizationHeader
        );

        SharedCredential sharedCredential =
                sharingService.shareCredentialAsManager(
                        credentialId,
                        managerId,
                        request.getSharedUserEmail(),
                        request.getPermissionLevel(),
                        request.getExpirationDate()
                );

        return ResponseEntity.ok(
                convertToResponse(sharedCredential)
        );
    }

    // =========================================================
    // 8. OWNER - REMOVE SHARED ACCESS
    // =========================================================
    //
    // DELETE
    // /api/sharing/credentials/{credentialId}/users/{sharedUserId}
    //
    // =========================================================

    @DeleteMapping(
            "/credentials/{credentialId}/users/{sharedUserId}"
    )
    public ResponseEntity<Void> removeSharedAccess(
            @PathVariable Long credentialId,
            @PathVariable Long sharedUserId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {

        Long ownerId = getUserIdFromToken(
                authorizationHeader
        );

        sharingService.removeSharedAccess(
                credentialId,
                ownerId,
                sharedUserId
        );

        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // 9. FULL MANAGEMENT - REMOVE SHARED ACCESS
    // =========================================================
    //
    // Owner OR FULL_MANAGEMENT user
    //
    // DELETE
    // /api/sharing/credentials/{credentialId}/manage/users/{sharedUserId}
    //
    // =========================================================

    @DeleteMapping(
            "/credentials/{credentialId}/manage/users/{sharedUserId}"
    )
    public ResponseEntity<Void> removeSharedAccessAsManager(
            @PathVariable Long credentialId,
            @PathVariable Long sharedUserId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {

        Long managerId = getUserIdFromToken(
                authorizationHeader
        );

        sharingService.removeSharedAccessAsManager(
                credentialId,
                managerId,
                sharedUserId
        );

        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // 10. REMOVE SHARED ACCESS USING SHARE ID
    // =========================================================
    //
    // DELETE /api/sharing/{shareId}
    //
    // Owner only
    //
    // =========================================================

    @DeleteMapping("/{shareId}")
    public ResponseEntity<Void> removeSharedAccessByShareId(
            @PathVariable Long shareId,
            @RequestHeader("Authorization") String authorizationHeader
    ) {

        Long ownerId = getUserIdFromToken(
                authorizationHeader
        );

        sharingService.removeSharedAccessByShareId(
                shareId,
                ownerId
        );

        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // 11. CHECK EXPIRED ACCESS
    // =========================================================
    //
    // POST /api/sharing/check-expired
    //
    // =========================================================

    @PostMapping("/check-expired")
    public ResponseEntity<String> checkExpiredAccess(
            @RequestHeader("Authorization") String authorizationHeader
    ) {

        // Verify authentication
        getUserIdFromToken(
                authorizationHeader
        );

        int removedCount =
                sharingService.removeExpiredAccess();

        return ResponseEntity.ok(
                "Expired shared access processed: "
                        + removedCount
        );
    }

    // =========================================================
    // 12. GET USER ID FROM JWT
    // =========================================================
    //
    // JWT subject contains EMAIL.
    //
    // Therefore:
    //
    // JWT
    //  ↓
    // extract email
    //  ↓
    // find user
    //  ↓
    // return user ID
    //
    // =========================================================

    private Long getUserIdFromToken(
            String authorizationHeader
    ) {

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            throw new RuntimeException(
                    "Missing or invalid Authorization header"
            );
        }

        String token =
                authorizationHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {

            throw new RuntimeException(
                    "Invalid or expired token"
            );
        }

        String email =
                jwtService.extractEmail(token);

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                )
                .getId();
    }

    // =========================================================
    // 13. CONVERT ENTITY TO RESPONSE
    // =========================================================

    private SharedCredentialResponse convertToResponse(
            SharedCredential sharedCredential
    ) {

        SharedCredentialResponse response =
                new SharedCredentialResponse();

        // Share ID
        response.setShareId(
                sharedCredential.getId()
        );

        // Credential details
        Credential credential =
                sharedCredential.getCredential();

        if (credential != null) {

            response.setCredentialId(
                    credential.getId()
            );

            response.setTitle(
                    credential.getTitle()
            );

            response.setUsername(
                    credential.getUsername()
            );

            response.setPassword(
                    credential.getPassword()
            );
        }

        // Owner details
        User owner =
                sharedCredential.getOwner();

        if (owner != null) {

            response.setOwnerId(
                    owner.getId()
            );

            response.setOwnerUsername(
                    owner.getUsername()
            );
        }

        // Shared user details
        User sharedUser =
                sharedCredential.getSharedUser();

        if (sharedUser != null) {

            response.setSharedUserId(
                    sharedUser.getId()
            );

            response.setSharedUserUsername(
                    sharedUser.getUsername()
            );
        }

        // Permission
        response.setPermissionLevel(
                sharedCredential.getPermissionLevel()
        );

        // Sharing status
        response.setSharingStatus(
                sharedCredential.getSharingStatus()
        );

        // Expiration
        response.setExpirationDate(
                sharedCredential.getExpirationDate()
        );

        return response;
    }

    // =========================================================
    // 14. UPDATE CREDENTIAL REQUEST
    // =========================================================

    public static class UpdateCredentialRequest {

        private String title;

        private String username;

        private String password;

        public UpdateCredentialRequest() {
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}