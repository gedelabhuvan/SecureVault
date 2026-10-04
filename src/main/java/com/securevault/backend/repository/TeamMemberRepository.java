package com.securevault.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.securevault.backend.entity.TeamMember;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    @EntityGraph(attributePaths = {"team", "user"})
    List<TeamMember> findByTeamId(Long teamId);

    @EntityGraph(attributePaths = {"team", "user"})
    Optional<TeamMember> findByTeamIdAndUserId(
            Long teamId,
            Long userId
    );

    boolean existsByTeamIdAndUserId(
            Long teamId,
            Long userId
    );

    @EntityGraph(attributePaths = {"team", "user"})
    List<TeamMember> findByUserId(Long userId);

    void deleteByTeamIdAndUserId(
            Long teamId,
            Long userId
    );
}