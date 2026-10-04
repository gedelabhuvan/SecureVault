package com.securevault.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.securevault.backend.dto.TeamCredentialAccessRequest;
import com.securevault.backend.dto.TeamCredentialAccessResponse;
import com.securevault.backend.entity.Credential;
import com.securevault.backend.entity.Team;
import com.securevault.backend.entity.TeamCredentialAccess;
import com.securevault.backend.entity.TeamMember;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.CredentialRepository;
import com.securevault.backend.repository.TeamCredentialAccessRepository;
import com.securevault.backend.repository.TeamMemberRepository;
import com.securevault.backend.repository.TeamRepository;
import com.securevault.backend.repository.UserRepository;

@Service
public class TeamCredentialAccessService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamCredentialAccessRepository accessRepository;
    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    public TeamCredentialAccessService(
            TeamRepository teamRepository,
            TeamMemberRepository teamMemberRepository,
            TeamCredentialAccessRepository accessRepository,
            CredentialRepository credentialRepository,
            UserRepository userRepository,
            EncryptionService encryptionService
    ) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.accessRepository = accessRepository;
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    // =========================================================
    // GRANT ACCESS
    // =========================================================

    public TeamCredentialAccessResponse grantAccess(
            Long teamId,
            Long credentialId,
            TeamCredentialAccessRequest request,
            String ownerEmail
    ) {

        User owner = getUserByEmail(ownerEmail);

        Team team = getTeamOwnedByUser(
                teamId,
                owner.getId()
        );

        Credential credential =
                credentialRepository.findById(credentialId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Credential not found"
                                )
                        );

        // Make sure the credential belongs to the owner
        if (!credential.getUser().getId().equals(owner.getId())) {
            throw new AccessDeniedException(
                    "You can only share credentials that you own"
            );
        }

        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException(
                    "User ID is required"
            );
        }

        if (request.getPermissionLevel() == null) {
            throw new IllegalArgumentException(
                    "Permission level is required"
            );
        }

        // =====================================================
        // EXPIRATION VALIDATION
        // =====================================================

        if (request.getExpirationDate() != null &&
                !request.getExpirationDate().isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Expiration date must be in the future"
            );
        }

        User member =
                userRepository.findById(request.getUserId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found"
                                )
                        );

        TeamMember teamMember =
                teamMemberRepository.findByTeamIdAndUserId(
                        teamId,
                        member.getId()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "User is not a member of this team"
                        )
                );

        if ("OWNER".equals(teamMember.getRole())) {
            throw new IllegalArgumentException(
                    "Team owner already owns the team"
            );
        }

        if (accessRepository
                .existsByTeamIdAndCredentialIdAndUserId(
                        teamId,
                        credentialId,
                        member.getId()
                )) {

            throw new IllegalArgumentException(
                    "User already has access to this credential"
            );
        }

        // =====================================================
        // CREATE ACCESS
        // =====================================================

        TeamCredentialAccess access =
                new TeamCredentialAccess();

        access.setTeam(team);
        access.setCredential(credential);
        access.setUser(member);

        access.setPermissionLevel(
                request.getPermissionLevel()
        );

        // Save expiration date
        access.setExpirationDate(
                request.getExpirationDate()
        );

        TeamCredentialAccess saved =
                accessRepository.save(access);

        return toResponse(saved);
    }

    // =========================================================
    // GET MY TEAM CREDENTIALS
    // =========================================================

    public List<TeamCredentialAccessResponse> getMyTeamCredentials(
            String email
    ) {

        User user = getUserByEmail(email);

        return accessRepository.findByUserId(user.getId())
                .stream()

                .filter(access ->
                        teamMemberRepository.existsByTeamIdAndUserId(
                                access.getTeam().getId(),
                                user.getId()
                        )
                )

                .filter(this::isAccessStillValid)

                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET CREDENTIAL ACCESS
    // =========================================================

    public List<TeamCredentialAccessResponse> getCredentialAccess(
            Long teamId,
            Long credentialId,
            String ownerEmail
    ) {

        User owner = getUserByEmail(ownerEmail);

        Team team = getTeamOwnedByUser(
                teamId,
                owner.getId()
        );

        Credential credential =
                credentialRepository.findById(credentialId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Credential not found"
                                )
                        );

        if (!credential.getUser().getId().equals(owner.getId())) {
            throw new AccessDeniedException(
                    "You do not own this credential"
            );
        }

        return accessRepository
                .findByTeamIdAndCredentialId(
                        team.getId(),
                        credential.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // GET CREDENTIAL FOR MEMBER
    // =========================================================

    public TeamCredentialAccessResponse getCredentialForMember(
            Long teamId,
            Long credentialId,
            String email
    ) {

        User user = getUserByEmail(email);

        TeamMember teamMember =
                teamMemberRepository.findByTeamIdAndUserId(
                        teamId,
                        user.getId()
                ).orElseThrow(() ->
                        new AccessDeniedException(
                                "You are not a member of this team"
                        )
                );

        // Team owner can access their own credentials
        if ("OWNER".equals(teamMember.getRole())) {

            Credential credential =
                    credentialRepository.findById(credentialId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Credential not found"
                                    )
                            );

            if (!credential.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException(
                        "You do not have access to this credential"
                );
            }

            return createOwnerResponse(
                    teamId,
                    credential
            );
        }

        // Normal team member
        TeamCredentialAccess access =
                getValidAccess(
                        teamId,
                        credentialId,
                        user.getId()
                );

        return toResponse(access);
    }

    // =========================================================
    // UPDATE CREDENTIAL
    // =========================================================

    public TeamCredentialAccessResponse updateCredential(
            Long teamId,
            Long credentialId,
            String title,
            String username,
            String password,
            String email
    ) {

        User user = getUserByEmail(email);

        TeamCredentialAccess access =
                getValidAccess(
                        teamId,
                        credentialId,
                        user.getId()
                );

        if (!canEdit(access.getPermissionLevel())) {
            throw new AccessDeniedException(
                    "You only have view-only access"
            );
        }

        Credential credential =
                credentialRepository.findById(credentialId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Credential not found"
                                )
                        );

        credential.setTitle(title);
        credential.setUsername(username);

        // Encrypt password before saving
        credential.setPassword(
                encryptionService.encrypt(password)
        );

        Credential updated =
                credentialRepository.save(credential);

        access.setCredential(updated);

        return toResponse(access);
    }

    // =========================================================
    // REMOVE ACCESS
    // =========================================================

    public void removeAccess(
            Long teamId,
            Long credentialId,
            Long userId,
            String ownerEmail
    ) {

        User owner = getUserByEmail(ownerEmail);

        getTeamOwnedByUser(
                teamId,
                owner.getId()
        );

        TeamCredentialAccess access =
                accessRepository
                        .findByTeamIdAndCredentialIdAndUserId(
                                teamId,
                                credentialId,
                                userId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Access record not found"
                                )
                        );

        accessRepository.delete(access);
    }

    // =========================================================
    // REMOVE MEMBER ACCESS
    // =========================================================

    public void removeMemberAccess(
            Long teamId,
            Long memberUserId,
            String ownerEmail
    ) {

        User owner = getUserByEmail(ownerEmail);

        getTeamOwnedByUser(
                teamId,
                owner.getId()
        );

        List<TeamCredentialAccess> accesses =
                accessRepository.findByTeamId(teamId)
                        .stream()
                        .filter(access ->
                                access.getUser()
                                        .getId()
                                        .equals(memberUserId)
                        )
                        .toList();

        accessRepository.deleteAll(accesses);
    }

    // =========================================================
    // CAN MANAGE ACCESS
    // =========================================================

    public boolean canManageAccess(
            Long teamId,
            Long credentialId,
            String email
    ) {

        User user = getUserByEmail(email);

        Credential credential =
                credentialRepository.findById(credentialId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Credential not found"
                                )
                        );

        // Credential owner can manage access
        if (credential.getUser().getId().equals(user.getId())) {
            return true;
        }

        TeamCredentialAccess access =
                accessRepository
                        .findByTeamIdAndCredentialIdAndUserId(
                                teamId,
                                credentialId,
                                user.getId()
                        )
                        .orElse(null);

        return access != null
                && access.getPermissionLevel()
                        == TeamCredentialAccess.PermissionLevel.FULL_MANAGEMENT;
    }

    // =========================================================
    // GET VALID ACCESS
    // =========================================================

    private TeamCredentialAccess getValidAccess(
            Long teamId,
            Long credentialId,
            Long userId
    ) {

        TeamCredentialAccess access =
                accessRepository
                        .findByTeamIdAndCredentialIdAndUserId(
                                teamId,
                                credentialId,
                                userId
                        )
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "You do not have access to this credential"
                                )
                        );

        // Check whether user is still a team member
        boolean isTeamMember =
                teamMemberRepository.existsByTeamIdAndUserId(
                        teamId,
                        userId
                );

        if (!isTeamMember) {
            throw new AccessDeniedException(
                    "You are no longer a member of this team"
            );
        }

        // Check expiration
        if (!isAccessStillValid(access)) {

            accessRepository.delete(access);

            throw new AccessDeniedException(
                    "Your access to this credential has expired"
            );
        }

        return access;
    }

    // =========================================================
    // CHECK EXPIRATION
    // =========================================================

    private boolean isAccessStillValid(
            TeamCredentialAccess access
    ) {

        LocalDateTime expirationDate =
                access.getExpirationDate();

        // Null means no expiration
        if (expirationDate == null) {
            return true;
        }

        return expirationDate.isAfter(
                LocalDateTime.now()
        );
    }

    // =========================================================
    // CAN EDIT
    // =========================================================

    private boolean canEdit(
            TeamCredentialAccess.PermissionLevel permission
    ) {

        return permission ==
                    TeamCredentialAccess.PermissionLevel.EDIT_ACCESS

                || permission ==
                    TeamCredentialAccess.PermissionLevel.FULL_MANAGEMENT;
    }

    // =========================================================
    // GET TEAM OWNED BY USER
    // =========================================================

    private Team getTeamOwnedByUser(
            Long teamId,
            Long ownerId
    ) {

        return teamRepository.findByIdAndOwnerId(
                teamId,
                ownerId
        ).orElseThrow(() ->
                new AccessDeniedException(
                        "You do not have permission to manage this team"
                )
        );
    }

    // =========================================================
    // GET USER BY EMAIL
    // =========================================================

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );
    }

    // =========================================================
    // CONVERT TO RESPONSE
    // =========================================================

    private TeamCredentialAccessResponse toResponse(
            TeamCredentialAccess access
    ) {

        Credential credential =
                access.getCredential();

        User member =
                access.getUser();

        Team team =
                access.getTeam();

        return new TeamCredentialAccessResponse(
                access.getId(),
                team.getId(),
                team.getName(),
                credential.getId(),
                credential.getTitle(),
                credential.getUsername(),

                encryptionService.decrypt(
                        credential.getPassword()
                ),

                member.getId(),
                member.getUsername(),
                member.getEmail(),

                access.getPermissionLevel(),

                // Return expiration date
                access.getExpirationDate(),

                access.getCreatedAt(),
                access.getUpdatedAt()
        );
    }

    // =========================================================
    // CREATE OWNER RESPONSE
    // =========================================================

    private TeamCredentialAccessResponse createOwnerResponse(
            Long teamId,
            Credential credential
    ) {

        Team team =
                teamRepository.findById(teamId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Team not found"
                                )
                        );

        User owner =
                credential.getUser();

        return new TeamCredentialAccessResponse(
                null,
                team.getId(),
                team.getName(),
                credential.getId(),
                credential.getTitle(),
                credential.getUsername(),

                encryptionService.decrypt(
                        credential.getPassword()
                ),

                owner.getId(),
                owner.getUsername(),
                owner.getEmail(),

                TeamCredentialAccess.PermissionLevel.FULL_MANAGEMENT,

                // Owner has no expiration
                null,

                credential.getCreatedAt(),
                credential.getUpdatedAt()
        );
    }
}