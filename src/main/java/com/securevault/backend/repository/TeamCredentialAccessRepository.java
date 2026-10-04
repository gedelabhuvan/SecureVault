package com.securevault.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.securevault.backend.entity.TeamCredentialAccess;

public interface TeamCredentialAccessRepository
        extends JpaRepository<TeamCredentialAccess, Long> {

    @EntityGraph(attributePaths = {"team", "credential", "user"})
    List<TeamCredentialAccess> findByTeamId(Long teamId);

    @EntityGraph(attributePaths = {"team", "credential", "user"})
    List<TeamCredentialAccess> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"team", "credential", "user"})
    Optional<TeamCredentialAccess> findByTeamIdAndCredentialIdAndUserId(
            Long teamId,
            Long credentialId,
            Long userId
    );

    @EntityGraph(attributePaths = {"team", "credential", "user"})
    List<TeamCredentialAccess> findByTeamIdAndCredentialId(
            Long teamId,
            Long credentialId
    );

    boolean existsByTeamIdAndCredentialIdAndUserId(
            Long teamId,
            Long credentialId,
            Long userId
    );

    void deleteByTeamIdAndCredentialIdAndUserId(
            Long teamId,
            Long credentialId,
            Long userId
    );
}