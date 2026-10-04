package com.securevault.backend.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.securevault.backend.dto.CredentialRequest;
import com.securevault.backend.dto.CredentialResponse;
import com.securevault.backend.entity.Credential;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.CredentialRepository;
import com.securevault.backend.repository.UserRepository;
import java.util.Comparator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

@Service
public class VaultService {

        @Transactional
public boolean setFavorite(Long id, boolean favorite, String email) {
    User user = getUserByEmail(email);

    Credential credential = credentialRepository
            .findByIdAndUser(id, user)
            .orElseThrow(() ->
                    new AccessDeniedException(
                            "You do not have permission to access this credential"
                    ));

    credential.setFavorite(favorite);
    credentialRepository.save(credential);

    return true;
}

    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    public VaultService(
            CredentialRepository credentialRepository,
            UserRepository userRepository,
            EncryptionService encryptionService
    ) {
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    public CredentialResponse addCredential(
            CredentialRequest request,
            String email
    ) {

        User user = getUserByEmail(email);

        Credential credential = new Credential();

        credential.setTitle(request.getTitle());
        credential.setUsername(request.getUsername());
        credential.setCategory(request.getCategory());

        // Encrypt password before storing it.
        credential.setPassword(
                encryptionService.encrypt(
                        request.getPassword()
                )
        );

        credential.setUser(user);

        Credential savedCredential =
                credentialRepository.save(credential);

        return toResponse(savedCredential);
    }

    @Cacheable(value = "credentials", key = "#email")
public List<CredentialResponse> getCredentials(String email) {
    User user = getUserByEmail(email);

    return credentialRepository.findByUser(user)
            .stream()
            .map(this::toResponse)
            .toList();
}

    public List<CredentialResponse> searchCredentials(
        String title,
        String email
) {

    User user = getUserByEmail(email);

    String searchTitle =
            title == null ? "" : title.trim();

    return credentialRepository
            .findByUserAndTitleContainingIgnoreCase(
                    user,
                    searchTitle
            )
            .stream()
            .map(this::toResponse)
            .toList();
}
public List<CredentialResponse> getFavoriteCredentials(String email) {
    User user = getUserByEmail(email);

    return credentialRepository
            .findByUserAndFavoriteTrue(user)
            .stream()
            .map(this::toResponse)
            .toList();
}

public List<CredentialResponse> getCredentialsWithFavoritesFirst(String email) {
    User user = getUserByEmail(email);

    List<Credential> credentials = credentialRepository.findByUser(user);

    credentials.sort(
            Comparator.comparing(Credential::isFavorite).reversed()
    );

    return credentials.stream()
            .map(this::toResponse)
            .toList();
}

public List<CredentialResponse> getSortedCredentials(
        String sortBy,
        String email
) {

    User user = getUserByEmail(email);

    List<Credential> credentials =
            credentialRepository.findByUser(user);

    if ("title".equalsIgnoreCase(sortBy)) {

        credentials.sort(
                Comparator.comparing(
                        Credential::getTitle,
                        String.CASE_INSENSITIVE_ORDER
                )
        );

    } else if ("newest".equalsIgnoreCase(sortBy)) {

        credentials.sort(
                Comparator.comparing(
                        Credential::getCreatedAt
                ).reversed()
        );

    } else if ("oldest".equalsIgnoreCase(sortBy)) {

        credentials.sort(
                Comparator.comparing(
                        Credential::getCreatedAt
                )
        );
    }

    return credentials.stream()
            .map(this::toResponse)
            .toList();
}

    public CredentialResponse updateCredential(
            Long id,
            CredentialRequest request,
            String email
    ) {

        User user = getUserByEmail(email);

        /*
         * Ownership check:
         * The credential must belong to the authenticated user.
         */
        Credential credential =
                credentialRepository.findByIdAndUser(id, user)
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "You do not have permission to access this credential"
                                )
                        );

        credential.setTitle(request.getTitle());
        credential.setUsername(request.getUsername());
        credential.setCategory(request.getCategory());

        // Encrypt the new password before storing it.
        credential.setPassword(
                encryptionService.encrypt(
                        request.getPassword()
                )
        );

        Credential updatedCredential =
                credentialRepository.save(credential);

        return toResponse(updatedCredential);
    }

    public void deleteCredential(
            Long id,
            String email
    ) {

        User user = getUserByEmail(email);

        /*
         * Ownership check:
         * The credential must belong to the authenticated user.
         */
        Credential credential =
                credentialRepository.findByIdAndUser(id, user)
                        .orElseThrow(() ->
                                new AccessDeniedException(
                                        "You do not have permission to access this credential"
                                )
                        );

        credentialRepository.delete(credential);
    }

    /*
     * Convert the database entity into a response.
     *
     * The password is decrypted only when it is being returned
     * to an already authenticated and authorized user.
     */
    private CredentialResponse toResponse(
            Credential credential
    ) {

        String decryptedPassword =
                encryptionService.decrypt(
                        credential.getPassword()
                );

        return new CredentialResponse(
                credential.getId(),
                credential.getTitle(),
                credential.getUsername(),
                decryptedPassword,
                credential.isFavorite(),
                credential.getCategory()

        );
    }

    public String generatePassword(int length) {

    if (length < 8 || length > 64) {
        throw new IllegalArgumentException(
                "Password length must be between 8 and 64"
        );
    }

    String characters =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "abcdefghijklmnopqrstuvwxyz" +
            "0123456789" +
            "!@#$%^&*()-_=+";

    StringBuilder password = new StringBuilder(length);

    java.security.SecureRandom random =
            new java.security.SecureRandom();

    for (int i = 0; i < length; i++) {
        password.append(
                characters.charAt(
                        random.nextInt(characters.length())
                )
        );
    }

    return password.toString();
}

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found"
                        )
                );
    }
}