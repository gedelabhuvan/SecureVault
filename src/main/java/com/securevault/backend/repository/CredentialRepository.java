package com.securevault.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.securevault.backend.entity.Credential;
import com.securevault.backend.entity.User;

public interface CredentialRepository
        extends JpaRepository<Credential, Long> {

    List<Credential> findByUser(User user);

    Optional<Credential> findByIdAndUser(Long id, User user);

    List<Credential> findByUserAndTitleContainingIgnoreCase(User user, String title);
    List<Credential> findByUserAndFavoriteTrue(User user);
}