package com.securevault.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.securevault.backend.entity.Credential;
import com.securevault.backend.entity.SharedCredential;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.CredentialRepository;
import com.securevault.backend.repository.SharedCredentialRepository;
import com.securevault.backend.repository.UserRepository;

@Service
public class SharingService {

    private final SharedCredentialRepository sharedCredentialRepository;
    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;

    public SharingService(
            SharedCredentialRepository sharedCredentialRepository,
            CredentialRepository credentialRepository,
            UserRepository userRepository
    ) {
        this.sharedCredentialRepository = sharedCredentialRepository;
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // SHARE CREDENTIAL - OWNER
    // =========================================================

    @Transactional
    public SharedCredential shareCredential(
            Long credentialId,
            Long ownerId,
            String sharedUserEmail,
            SharedCredential.PermissionLevel permissionLevel,
            LocalDateTime expirationDate
    ) {

        Credential credential =
                getCredential(credentialId);

        // Make sure current user is the owner
        if (credential.getUser() == null
                || !credential.getUser().getId().equals(ownerId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not the owner of this credential"
            );
        }

        return createOrUpdateShare(
                credential,
                credential.getUser(),
                sharedUserEmail,
                permissionLevel,
                expirationDate
        );
    }


    // =========================================================
    // SHARE CREDENTIAL - FULL MANAGEMENT USER
    // =========================================================

    @Transactional
    public SharedCredential shareCredentialAsManager(
            Long credentialId,
            Long managerId,
            String sharedUserEmail,
            SharedCredential.PermissionLevel permissionLevel,
            LocalDateTime expirationDate
    ) {

        Credential credential =
                getCredential(credentialId);

        // Owner automatically has full management
        if (credential.getUser() != null
                && credential.getUser().getId().equals(managerId)) {

            return createOrUpdateShare(
                    credential,
                    credential.getUser(),
                    sharedUserEmail,
                    permissionLevel,
                    expirationDate
            );
        }

        // Find manager's existing access
        SharedCredential managerAccess =
                sharedCredentialRepository
                        .findByCredentialIdAndSharedUserId(
                                credentialId,
                                managerId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "You do not have access to this credential"
                                )
                        );

        checkAccessIsValid(managerAccess);

        if (managerAccess.getPermissionLevel()
                != SharedCredential.PermissionLevel.FULL_MANAGEMENT) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Full Management permission is required"
            );
        }

        // Actual owner remains the owner
        return createOrUpdateShare(
                credential,
                credential.getUser(),
                sharedUserEmail,
                permissionLevel,
                expirationDate
        );
    }


    // =========================================================
    // CREATE OR UPDATE EXISTING SHARE
    // =========================================================

    private SharedCredential createOrUpdateShare(
            Credential credential,
            User owner,
            String sharedUserEmail,
            SharedCredential.PermissionLevel permissionLevel,
            LocalDateTime expirationDate
    ) {

        if (sharedUserEmail == null
                || sharedUserEmail.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Shared user email is required"
            );
        }

        if (permissionLevel == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Permission level is required"
            );
        }

        if (expirationDate != null
                && !expirationDate.isAfter(LocalDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Expiration date must be in the future"
            );
        }

        User sharedUser =
                userRepository
                        .findByEmail(sharedUserEmail.trim())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Shared user not found"
                                )
                        );

        // Cannot share with yourself
        if (sharedUser.getId().equals(owner.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot share a credential with yourself"
            );
        }

        /*
         * IMPORTANT:
         *
         * If a share already exists, UPDATE it.
         *
         * This allows:
         *
         * VIEW_ONLY
         *      ↓
         * EDIT_ACCESS
         *
         * or:
         *
         * EDIT_ACCESS
         *      ↓
         * FULL_MANAGEMENT
         */
        SharedCredential sharedCredential =
                sharedCredentialRepository
                        .findByCredentialIdAndSharedUserId(
                                credential.getId(),
                                sharedUser.getId()
                        )
                        .orElse(null);

        if (sharedCredential == null) {

            sharedCredential =
                    new SharedCredential();

            sharedCredential.setCredential(credential);
            sharedCredential.setOwner(owner);
            sharedCredential.setSharedUser(sharedUser);

        } else {

            // Existing share found.
            // Reactivate it if it was revoked/expired.
            sharedCredential.setOwner(owner);
            sharedCredential.setCredential(credential);
            sharedCredential.setSharedUser(sharedUser);
        }

        sharedCredential.setPermissionLevel(
                permissionLevel
        );

        sharedCredential.setSharingStatus(
                SharedCredential.SharingStatus.ACTIVE
        );

        sharedCredential.setExpirationDate(
                expirationDate
        );

        return sharedCredentialRepository.save(
                sharedCredential
        );
    }


    // =========================================================
    // GET SHARED WITH ME
    // =========================================================

    @Transactional
    public List<SharedCredential> getSharedWithMe(
            Long userId
    ) {

        List<SharedCredential> shares =
                sharedCredentialRepository
                        .findBySharedUserId(userId);

        for (SharedCredential share : shares) {

            if (isExpired(share)
                    && share.getSharingStatus()
                    == SharedCredential.SharingStatus.ACTIVE) {

                share.setSharingStatus(
                        SharedCredential.SharingStatus.REVOKED
                );

                sharedCredentialRepository.save(share);
            }
        }

        return shares.stream()
                .filter(this::isAccessValid)
                .toList();
    }


    // =========================================================
    // GET SHARES FOR A CREDENTIAL
    // OWNER OR FULL MANAGEMENT USER
    // =========================================================

    @Transactional
    public List<SharedCredential> getCredentialShares(
            Long credentialId,
            Long requesterId
    ) {

        Credential credential =
                getCredential(credentialId);

        // Owner can always view sharing details.
        if (credential.getUser() != null
                && credential.getUser().getId().equals(requesterId)) {

            return sharedCredentialRepository
                    .findByCredentialId(credentialId);
        }

        // A shared user must have valid FULL_MANAGEMENT access
        // to view and manage the credential's sharing details.
        SharedCredential managerAccess =
                sharedCredentialRepository
                        .findByCredentialIdAndSharedUserId(
                                credentialId,
                                requesterId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "You do not have access to this credential"
                                )
                        );

        checkAccessIsValid(managerAccess);

        if (managerAccess.getPermissionLevel()
                != SharedCredential.PermissionLevel.FULL_MANAGEMENT) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Full Management permission is required to view sharing details"
            );
        }

        return sharedCredentialRepository
                .findByCredentialId(credentialId);
    }


    // =========================================================
    // GET ONE SHARED CREDENTIAL
    // =========================================================

    @Transactional(readOnly = true)
    public SharedCredential getSharedCredential(
            Long credentialId,
            Long userId
    ) {

        Credential credential =
                getCredential(credentialId);

        // Owner
        if (credential.getUser() != null
                && credential.getUser().getId().equals(userId)) {

            return createOwnerAccessView(credential);
        }

        // Shared user
        SharedCredential sharedCredential =
                sharedCredentialRepository
                        .findByCredentialIdAndSharedUserId(
                                credentialId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "You do not have access to this credential"
                                )
                        );

        checkAccessIsValid(sharedCredential);

        return sharedCredential;
    }


    // =========================================================
    // VIEW CREDENTIAL
    // =========================================================

    @Transactional(readOnly = true)
    public Credential getCredentialForViewing(
            Long credentialId,
            Long userId
    ) {

        Credential credential =
                getCredential(credentialId);

        // Owner
        if (credential.getUser() != null
                && credential.getUser().getId().equals(userId)) {

            return credential;
        }

        // Shared user
        SharedCredential sharedCredential =
                sharedCredentialRepository
                        .findByCredentialIdAndSharedUserId(
                                credentialId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "You do not have access to this credential"
                                )
                        );

        checkAccessIsValid(sharedCredential);

        return credential;
    }


    // =========================================================
    // UPDATE CREDENTIAL
    // =========================================================

    @Transactional
    public Credential updateSharedCredential(
            Long credentialId,
            Long userId,
            String title,
            String username,
            String password
    ) {

        Credential credential =
                getCredential(credentialId);

        // Owner can always edit
        if (credential.getUser() != null
                && credential.getUser().getId().equals(userId)) {

            updateCredentialFields(
                    credential,
                    title,
                    username,
                    password
            );

            return credentialRepository.save(
                    credential
            );
        }

        // Check shared access
        SharedCredential sharedCredential =
                sharedCredentialRepository
                        .findByCredentialIdAndSharedUserId(
                                credentialId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "You do not have access to this credential"
                                )
                        );

        checkAccessIsValid(sharedCredential);

        if (!canEdit(
                sharedCredential.getPermissionLevel()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to edit this credential"
            );
        }

        updateCredentialFields(
                credential,
                title,
                username,
                password
        );

        return credentialRepository.save(
                credential
        );
    }


    // =========================================================
    // REMOVE ACCESS - OWNER
    // =========================================================

    @Transactional
    public void removeSharedAccess(
            Long credentialId,
            Long ownerId,
            Long sharedUserId
    ) {

        Credential credential =
                getCredential(credentialId);

        if (credential.getUser() == null
                || !credential.getUser().getId().equals(ownerId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the credential owner can remove access"
            );
        }

        SharedCredential sharedCredential =
                sharedCredentialRepository
                        .findByCredentialIdAndSharedUserId(
                                credentialId,
                                sharedUserId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Shared access not found"
                                )
                        );

        sharedCredentialRepository.delete(
                sharedCredential
        );
    }


    // =========================================================
    // REMOVE ACCESS - FULL MANAGEMENT
    // =========================================================

    @Transactional
    public void removeSharedAccessAsManager(
            Long credentialId,
            Long managerId,
            Long sharedUserId
    ) {

        Credential credential =
                getCredential(credentialId);

        // Owner
        if (credential.getUser() != null
                && credential.getUser().getId().equals(managerId)) {

            removeSharedAccess(
                    credentialId,
                    managerId,
                    sharedUserId
            );

            return;
        }

        // Check manager access
        SharedCredential managerAccess =
                sharedCredentialRepository
                        .findByCredentialIdAndSharedUserId(
                                credentialId,
                                managerId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "You do not have access to this credential"
                                )
                        );

        checkAccessIsValid(managerAccess);

        if (managerAccess.getPermissionLevel()
                != SharedCredential.PermissionLevel.FULL_MANAGEMENT) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Full Management permission is required"
            );
        }

        SharedCredential targetShare =
                sharedCredentialRepository
                        .findByCredentialIdAndSharedUserId(
                                credentialId,
                                sharedUserId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Shared access not found"
                                )
                        );

        sharedCredentialRepository.delete(
                targetShare
        );
    }


    // =========================================================
    // REMOVE ACCESS USING SHARE ID
    // =========================================================

    @Transactional
    public void removeSharedAccessByShareId(
            Long shareId,
            Long ownerId
    ) {

        SharedCredential sharedCredential =
                sharedCredentialRepository
                        .findById(shareId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Shared access not found"
                                )
                        );

        if (sharedCredential.getOwner() == null
                || !sharedCredential
                .getOwner()
                .getId()
                .equals(ownerId)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the credential owner can remove access"
            );
        }

        sharedCredentialRepository.delete(
                sharedCredential
        );
    }


    // =========================================================
    // PROCESS EXPIRED ACCESS
    // =========================================================

    @Transactional
    public int removeExpiredAccess() {

        List<SharedCredential> shares =
                sharedCredentialRepository.findAll();

        int removedCount = 0;

        for (SharedCredential share : shares) {

            if (share.getSharingStatus()
                    == SharedCredential.SharingStatus.ACTIVE
                    && isExpired(share)) {

                share.setSharingStatus(
                        SharedCredential.SharingStatus.REVOKED
                );

                sharedCredentialRepository.save(
                        share
                );

                removedCount++;
            }
        }

        return removedCount;
    }


    // =========================================================
    // GET CREDENTIAL
    // =========================================================

    private Credential getCredential(
            Long credentialId
    ) {

        return credentialRepository
                .findById(credentialId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Credential not found"
                        )
                );
    }


    // =========================================================
    // ACCESS VALIDATION
    // =========================================================

    private boolean isAccessValid(
            SharedCredential sharedCredential
    ) {

        if (sharedCredential.getSharingStatus()
                != SharedCredential.SharingStatus.ACTIVE) {

            return false;
        }

        if (isExpired(sharedCredential)) {

            sharedCredential.setSharingStatus(
                    SharedCredential.SharingStatus.REVOKED
            );

            sharedCredentialRepository.save(
                    sharedCredential
            );

            return false;
        }

        return true;
    }


    private boolean isExpired(
            SharedCredential sharedCredential
    ) {

        return sharedCredential.getExpirationDate() != null
                && !sharedCredential
                .getExpirationDate()
                .isAfter(LocalDateTime.now());
    }


    private void checkAccessIsValid(
            SharedCredential sharedCredential
    ) {

        if (!isAccessValid(sharedCredential)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Shared access has expired or has been revoked"
            );
        }
    }


    // =========================================================
    // PERMISSION CHECK
    // =========================================================

    private boolean canEdit(
            SharedCredential.PermissionLevel permissionLevel
    ) {

        return permissionLevel
                == SharedCredential.PermissionLevel.EDIT_ACCESS
                || permissionLevel
                == SharedCredential.PermissionLevel.FULL_MANAGEMENT;
    }


    // =========================================================
    // UPDATE CREDENTIAL FIELDS
    // =========================================================

    private void updateCredentialFields(
            Credential credential,
            String title,
            String username,
            String password
    ) {

        if (title != null && !title.isBlank()) {
            credential.setTitle(title);
        }

        if (username != null && !username.isBlank()) {
            credential.setUsername(username);
        }

        if (password != null && !password.isBlank()) {
            credential.setPassword(password);
        }
    }


    // =========================================================
    // OWNER ACCESS VIEW
    // =========================================================

    private SharedCredential createOwnerAccessView(
            Credential credential
    ) {

        SharedCredential ownerAccess =
                new SharedCredential();

        ownerAccess.setCredential(
                credential
        );

        ownerAccess.setOwner(
                credential.getUser()
        );

        ownerAccess.setSharedUser(
                credential.getUser()
        );

        ownerAccess.setPermissionLevel(
                SharedCredential.PermissionLevel.FULL_MANAGEMENT
        );

        ownerAccess.setSharingStatus(
                SharedCredential.SharingStatus.ACTIVE
        );

        ownerAccess.setExpirationDate(
                null
        );

        return ownerAccess;
    }
}