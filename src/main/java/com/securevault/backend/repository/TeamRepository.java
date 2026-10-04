package com.securevault.backend.repository;

import com.securevault.backend.entity.Team;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @EntityGraph(attributePaths = {"owner"})
    List<Team> findByOwnerId(Long ownerId);

    @EntityGraph(attributePaths = {"owner"})
    Optional<Team> findByIdAndOwnerId(
            Long teamId,
            Long ownerId
    );

    boolean existsByNameAndOwnerId(
            String name,
            Long ownerId
    );
}
