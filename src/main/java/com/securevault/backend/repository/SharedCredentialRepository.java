package com.securevault.backend.repository;

import com.securevault.backend.entity.SharedCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SharedCredentialRepository
        extends JpaRepository<SharedCredential, Long> {

    List<SharedCredential> findBySharedUserId(Long sharedUserId);

    List<SharedCredential> findByOwnerId(Long ownerId);

    List<SharedCredential> findByCredentialId(Long credentialId);

    Optional<SharedCredential> findByCredentialIdAndSharedUserId(
            Long credentialId,
            Long sharedUserId
    );

    Optional<SharedCredential> findByCredentialIdAndOwnerId(
            Long credentialId,
            Long ownerId
    );

    boolean existsByCredentialIdAndSharedUserId(
            Long credentialId,
            Long sharedUserId
    );

    void deleteByCredentialIdAndSharedUserId(
            Long credentialId,
            Long sharedUserId
    );
}