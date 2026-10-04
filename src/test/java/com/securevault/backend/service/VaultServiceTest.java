package com.securevault.backend.service;

import com.securevault.backend.dto.CredentialRequest;
import com.securevault.backend.dto.CredentialResponse;
import com.securevault.backend.entity.Credential;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.CredentialRepository;
import com.securevault.backend.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VaultServiceTest {

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private VaultService vaultService;


    // =========================================================
    // ADD CREDENTIAL
    // =========================================================

    @Test
    void addCredential_shouldCreateCredentialSuccessfully() {

        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@gmail.com");

        CredentialRequest request = new CredentialRequest();
        request.setTitle("Gmail");
        request.setUsername("test@gmail.com");
        request.setPassword("password123");

        Credential savedCredential = new Credential();
        savedCredential.setId(10L);
        savedCredential.setTitle("Gmail");
        savedCredential.setUsername("test@gmail.com");
        savedCredential.setPassword("encryptedPassword");
        savedCredential.setUser(user);

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(encryptionService.encrypt("password123"))
                .thenReturn("encryptedPassword");

        when(credentialRepository.save(any(Credential.class)))
                .thenReturn(savedCredential);

        when(encryptionService.decrypt("encryptedPassword"))
                .thenReturn("password123");

        CredentialResponse response =
                vaultService.addCredential(
                        request,
                        "test@gmail.com"
                );

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Gmail", response.getTitle());
        assertEquals("test@gmail.com", response.getUsername());
        assertEquals("password123", response.getPassword());

        verify(encryptionService)
                .encrypt("password123");

        verify(credentialRepository)
                .save(any(Credential.class));
    }


    @Test
    void addCredential_shouldRejectUnknownUser() {

        CredentialRequest request = new CredentialRequest();
        request.setTitle("Gmail");
        request.setUsername("test@gmail.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> vaultService.addCredential(
                                request,
                                "unknown@gmail.com"
                        )
                );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(credentialRepository, never())
                .save(any(Credential.class));

        verify(encryptionService, never())
                .encrypt(anyString());
    }


    // =========================================================
    // GET CREDENTIALS
    // =========================================================

    @Test
    void getCredentials_shouldReturnUserCredentials() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");

        Credential credential1 = new Credential();
        credential1.setId(1L);
        credential1.setTitle("Gmail");
        credential1.setUsername("user1");
        credential1.setPassword("encrypted1");
        credential1.setUser(user);

        Credential credential2 = new Credential();
        credential2.setId(2L);
        credential2.setTitle("GitHub");
        credential2.setUsername("user2");
        credential2.setPassword("encrypted2");
        credential2.setUser(user);

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository.findByUser(user))
                .thenReturn(List.of(credential1, credential2));

        when(encryptionService.decrypt("encrypted1"))
                .thenReturn("password1");

        when(encryptionService.decrypt("encrypted2"))
                .thenReturn("password2");

        List<CredentialResponse> responses =
                vaultService.getCredentials("test@gmail.com");

        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals("Gmail", responses.get(0).getTitle());
        assertEquals("password1", responses.get(0).getPassword());

        assertEquals("GitHub", responses.get(1).getTitle());
        assertEquals("password2", responses.get(1).getPassword());

        verify(credentialRepository)
                .findByUser(user);
    }


    @Test
    void getCredentials_shouldReturnEmptyListWhenNoCredentialsExist() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository.findByUser(user))
                .thenReturn(List.of());

        List<CredentialResponse> responses =
                vaultService.getCredentials("test@gmail.com");

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(credentialRepository)
                .findByUser(user);
    }


    // =========================================================
    // UPDATE CREDENTIAL
    // =========================================================

    @Test
    void updateCredential_shouldUpdateCredentialSuccessfully() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");

        Credential credential = new Credential();
        credential.setId(10L);
        credential.setTitle("Old Title");
        credential.setUsername("olduser");
        credential.setPassword("oldEncrypted");
        credential.setUser(user);

        CredentialRequest request = new CredentialRequest();
        request.setTitle("Updated Title");
        request.setUsername("newuser");
        request.setPassword("newPassword");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository.findByIdAndUser(10L, user))
                .thenReturn(Optional.of(credential));

        when(encryptionService.encrypt("newPassword"))
                .thenReturn("newEncrypted");

        when(credentialRepository.save(credential))
                .thenReturn(credential);

        when(encryptionService.decrypt("newEncrypted"))
                .thenReturn("newPassword");

        CredentialResponse response =
                vaultService.updateCredential(
                        10L,
                        request,
                        "test@gmail.com"
                );

        assertEquals("Updated Title", response.getTitle());
        assertEquals("newuser", response.getUsername());
        assertEquals("newPassword", response.getPassword());

        assertEquals("Updated Title", credential.getTitle());
        assertEquals("newuser", credential.getUsername());
        assertEquals("newEncrypted", credential.getPassword());

        verify(credentialRepository)
                .save(credential);
    }


    @Test
    void updateCredential_shouldRejectUnauthorizedUser() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");

        CredentialRequest request = new CredentialRequest();
        request.setTitle("Updated");
        request.setUsername("user");
        request.setPassword("password");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository.findByIdAndUser(10L, user))
                .thenReturn(Optional.empty());

        AccessDeniedException exception =
                assertThrows(
                        AccessDeniedException.class,
                        () -> vaultService.updateCredential(
                                10L,
                                request,
                                "test@gmail.com"
                        )
                );

        assertEquals(
                "You do not have permission to access this credential",
                exception.getMessage()
        );

        verify(credentialRepository, never())
                .save(any(Credential.class));

        verify(encryptionService, never())
                .encrypt(anyString());
    }


    // =========================================================
    // DELETE CREDENTIAL
    // =========================================================

    @Test
    void deleteCredential_shouldDeleteOwnedCredential() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");

        Credential credential = new Credential();
        credential.setId(10L);
        credential.setTitle("Gmail");
        credential.setUser(user);

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository.findByIdAndUser(10L, user))
                .thenReturn(Optional.of(credential));

        vaultService.deleteCredential(
                10L,
                "test@gmail.com"
        );

        verify(credentialRepository)
                .delete(credential);
    }


    @Test
    void deleteCredential_shouldRejectUnauthorizedUser() {

        User user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(credentialRepository.findByIdAndUser(10L, user))
                .thenReturn(Optional.empty());

        AccessDeniedException exception =
                assertThrows(
                        AccessDeniedException.class,
                        () -> vaultService.deleteCredential(
                                10L,
                                "test@gmail.com"
                        )
                );

        assertEquals(
                "You do not have permission to access this credential",
                exception.getMessage()
        );

        verify(credentialRepository, never())
                .delete(any(Credential.class));
    }
}