package com.securevault.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.securevault.backend.entity.Credential;
import com.securevault.backend.entity.SharedCredential;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.CredentialRepository;
import com.securevault.backend.repository.SharedCredentialRepository;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.service.SharingService;

@ExtendWith(MockitoExtension.class)
class SharingServiceTest {

    @Mock
    private SharedCredentialRepository sharedCredentialRepository;

    @Mock
    private CredentialRepository credentialRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SharingService sharingService;

    private User owner;
    private User sharedUser;
    private User anotherUser;
    private Credential credential;
    private SharedCredential sharedCredential;

    @BeforeEach
    void setUp() {

        owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@test.com");

        sharedUser = new User();
        sharedUser.setId(2L);
        sharedUser.setEmail("shared@test.com");

        anotherUser = new User();
        anotherUser.setId(3L);
        anotherUser.setEmail("another@test.com");

        credential = new Credential();
        credential.setId(10L);
        credential.setTitle("Gmail");
        credential.setUsername("owner@gmail.com");
        credential.setPassword("password123");
        credential.setUser(owner);

        sharedCredential = new SharedCredential();
        sharedCredential.setCredential(credential);
        sharedCredential.setOwner(owner);
        sharedCredential.setSharedUser(sharedUser);
        sharedCredential.setPermissionLevel(
                SharedCredential.PermissionLevel.VIEW_ONLY
        );
        sharedCredential.setSharingStatus(
                SharedCredential.SharingStatus.ACTIVE
        );
        sharedCredential.setExpirationDate(null);
    }

    // =========================================================
    // 1. SHARE CREDENTIAL - SUCCESS
    // =========================================================

    @Test
    void shareCredential_success() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(userRepository.findByEmail("shared@test.com"))
                .thenReturn(Optional.of(sharedUser));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 2L))
                .thenReturn(Optional.empty());

        when(sharedCredentialRepository.save(any(SharedCredential.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SharedCredential result =
                sharingService.shareCredential(
                        10L,
                        1L,
                        "shared@test.com",
                        SharedCredential.PermissionLevel.VIEW_ONLY,
                        null
                );

        assertNotNull(result);
        assertEquals(
                SharedCredential.PermissionLevel.VIEW_ONLY,
                result.getPermissionLevel()
        );
        assertEquals(
                SharedCredential.SharingStatus.ACTIVE,
                result.getSharingStatus()
        );

        verify(sharedCredentialRepository).save(any(SharedCredential.class));
    }

    // =========================================================
    // 2. SHARE CREDENTIAL - NOT OWNER
    // =========================================================

    @Test
    void shareCredential_notOwner_throwsForbidden() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> sharingService.shareCredential(
                                10L,
                                3L,
                                "shared@test.com",
                                SharedCredential.PermissionLevel.VIEW_ONLY,
                                null
                        )
                );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verifyNoInteractions(userRepository);
        verify(sharedCredentialRepository, never())
                .save(any());
    }

    // =========================================================
    // 3. SHARE CREDENTIAL - INVALID EMAIL
    // =========================================================

    @Test
    void shareCredential_blankEmail_throwsBadRequest() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> sharingService.shareCredential(
                                10L,
                                1L,
                                "   ",
                                SharedCredential.PermissionLevel.VIEW_ONLY,
                                null
                        )
                );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    // =========================================================
    // 4. SHARE CREDENTIAL - NULL PERMISSION
    // =========================================================

    @Test
    void shareCredential_nullPermission_throwsBadRequest() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> sharingService.shareCredential(
                                10L,
                                1L,
                                "shared@test.com",
                                null,
                                null
                        )
                );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    // =========================================================
    // 5. SHARE CREDENTIAL - EXPIRED DATE
    // =========================================================

    @Test
    void shareCredential_pastExpirationDate_throwsBadRequest() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        LocalDateTime past =
                LocalDateTime.now().minusDays(1);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> sharingService.shareCredential(
                                10L,
                                1L,
                                "shared@test.com",
                                SharedCredential.PermissionLevel.VIEW_ONLY,
                                past
                        )
                );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    // =========================================================
    // 6. SHARE WITH NON-EXISTING USER
    // =========================================================

    @Test
    void shareCredential_userNotFound_throwsNotFound() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> sharingService.shareCredential(
                                10L,
                                1L,
                                "unknown@test.com",
                                SharedCredential.PermissionLevel.VIEW_ONLY,
                                null
                        )
                );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    // =========================================================
    // 7. SHARE WITH YOURSELF
    // =========================================================

    @Test
    void shareCredential_selfSharing_throwsBadRequest() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(userRepository.findByEmail("owner@test.com"))
                .thenReturn(Optional.of(owner));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> sharingService.shareCredential(
                                10L,
                                1L,
                                "owner@test.com",
                                SharedCredential.PermissionLevel.VIEW_ONLY,
                                null
                        )
                );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    // =========================================================
    // 8. GET SHARED CREDENTIAL - SUCCESS
    // =========================================================

    @Test
    void getSharedCredential_success() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 2L))
                .thenReturn(Optional.of(sharedCredential));

        SharedCredential result =
                sharingService.getSharedCredential(10L, 2L);

        assertNotNull(result);
        assertEquals(
                SharedCredential.PermissionLevel.VIEW_ONLY,
                result.getPermissionLevel()
        );
    }

    // =========================================================
    // 9. GET SHARED CREDENTIAL - UNAUTHORIZED
    // =========================================================

    @Test
    void getSharedCredential_unauthorized_throwsForbidden() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 3L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> sharingService.getSharedCredential(10L, 3L)
                );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    // =========================================================
    // 10. VIEW CREDENTIAL - OWNER
    // =========================================================

    @Test
    void getCredentialForViewing_owner_success() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        Credential result =
                sharingService.getCredentialForViewing(10L, 1L);

        assertNotNull(result);
        assertEquals("Gmail", result.getTitle());

        verify(sharedCredentialRepository, never())
                .findByCredentialIdAndSharedUserId(any(), any());
    }

    // =========================================================
    // 11. VIEW CREDENTIAL - SHARED USER
    // =========================================================

    @Test
    void getCredentialForViewing_sharedUser_success() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 2L))
                .thenReturn(Optional.of(sharedCredential));

        Credential result =
                sharingService.getCredentialForViewing(10L, 2L);

        assertNotNull(result);
        assertEquals("Gmail", result.getTitle());
    }

    // =========================================================
    // 12. UPDATE - OWNER
    // =========================================================

    @Test
    void updateSharedCredential_owner_success() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(credentialRepository.save(any(Credential.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Credential result =
                sharingService.updateSharedCredential(
                        10L,
                        1L,
                        "Updated Gmail",
                        "new@gmail.com",
                        "newPassword"
                );

        assertEquals("Updated Gmail", result.getTitle());
        assertEquals("new@gmail.com", result.getUsername());
        assertEquals("newPassword", result.getPassword());

        verify(credentialRepository).save(credential);
    }

    // =========================================================
    // 13. UPDATE - VIEW ONLY USER
    // =========================================================

    @Test
    void updateSharedCredential_viewOnly_throwsForbidden() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 2L))
                .thenReturn(Optional.of(sharedCredential));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> sharingService.updateSharedCredential(
                                10L,
                                2L,
                                "Changed",
                                "changed@test.com",
                                "password"
                        )
                );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(credentialRepository, never()).save(any());
    }

    // =========================================================
    // 14. UPDATE - EDIT ACCESS USER
    // =========================================================

    @Test
    void updateSharedCredential_editAccess_success() {

        sharedCredential.setPermissionLevel(
                SharedCredential.PermissionLevel.EDIT_ACCESS
        );

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 2L))
                .thenReturn(Optional.of(sharedCredential));

        when(credentialRepository.save(any(Credential.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Credential result =
                sharingService.updateSharedCredential(
                        10L,
                        2L,
                        "Edited",
                        "edited@test.com",
                        "editedPassword"
                );

        assertEquals("Edited", result.getTitle());
        assertEquals("edited@test.com", result.getUsername());
        assertEquals("editedPassword", result.getPassword());

        verify(credentialRepository).save(credential);
    }

    // =========================================================
    // 15. REMOVE ACCESS - OWNER
    // =========================================================

    @Test
    void removeSharedAccess_owner_success() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 2L))
                .thenReturn(Optional.of(sharedCredential));

        sharingService.removeSharedAccess(10L, 1L, 2L);

        verify(sharedCredentialRepository)
                .delete(sharedCredential);
    }

    // =========================================================
    // 16. REMOVE ACCESS - NOT OWNER
    // =========================================================

    @Test
    void removeSharedAccess_notOwner_throwsForbidden() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> sharingService.removeSharedAccess(
                                10L,
                                3L,
                                2L
                        )
                );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(sharedCredentialRepository, never())
                .delete(any());
    }

    // =========================================================
    // 17. REMOVE ACCESS - SHARE NOT FOUND
    // =========================================================

    @Test
    void removeSharedAccess_shareNotFound_throwsNotFound() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 2L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> sharingService.removeSharedAccess(
                                10L,
                                1L,
                                2L
                        )
                );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    // =========================================================
    // 18. REMOVE EXPIRED ACCESS
    // =========================================================

    @Test
    void removeExpiredAccess_expiredShare_isRevoked() {

        SharedCredential expiredShare =
                new SharedCredential();

        expiredShare.setSharingStatus(
                SharedCredential.SharingStatus.ACTIVE
        );

        expiredShare.setExpirationDate(
                LocalDateTime.now().minusDays(1)
        );

        when(sharedCredentialRepository.findAll())
                .thenReturn(List.of(expiredShare));

        int result =
                sharingService.removeExpiredAccess();

        assertEquals(1, result);

        assertEquals(
                SharedCredential.SharingStatus.REVOKED,
                expiredShare.getSharingStatus()
        );

        verify(sharedCredentialRepository)
                .save(expiredShare);
    }

    // =========================================================
    // 19. REMOVE EXPIRED ACCESS - ACTIVE NON-EXPIRED
    // =========================================================

    @Test
    void removeExpiredAccess_activeShareNotExpired_notRemoved() {

        SharedCredential activeShare =
                new SharedCredential();

        activeShare.setSharingStatus(
                SharedCredential.SharingStatus.ACTIVE
        );

        activeShare.setExpirationDate(
                LocalDateTime.now().plusDays(2)
        );

        when(sharedCredentialRepository.findAll())
                .thenReturn(List.of(activeShare));

        int result=sharingService.removeExpiredAccess();

        assertEquals(0, result);

        assertEquals(
                SharedCredential.SharingStatus.ACTIVE,
                activeShare.getSharingStatus()
        );

        verify(sharedCredentialRepository, never())
                .save(activeShare);
    }

    // =========================================================
    // 20. GET SHARED WITH ME
    // =========================================================

    @Test
    void getSharedWithMe_returnsActiveShares() {

        when(sharedCredentialRepository.findBySharedUserId(2L))
                .thenReturn(List.of(sharedCredential));

        List<SharedCredential> result =
                sharingService.getSharedWithMe(2L);

        assertEquals(1, result.size());
        assertSame(sharedCredential, result.get(0));
    }

    // =========================================================
    // 21. REVOKED ACCESS IS NOT RETURNED
    // =========================================================

    @Test
    void getSharedWithMe_revokedShare_isFiltered() {

        sharedCredential.setSharingStatus(
                SharedCredential.SharingStatus.REVOKED
        );

        when(sharedCredentialRepository.findBySharedUserId(2L))
                .thenReturn(List.of(sharedCredential));

        List<SharedCredential> result =
                sharingService.getSharedWithMe(2L);

        assertTrue(result.isEmpty());
    }

    // =========================================================
    // 22. GET CREDENTIAL SHARES - OWNER
    // =========================================================

    @Test
    void getCredentialShares_owner_success() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository.findByCredentialId(10L))
                .thenReturn(List.of(sharedCredential));

        List<SharedCredential> result =
                sharingService.getCredentialShares(10L, 1L);

        assertEquals(1, result.size());
        assertSame(sharedCredential, result.get(0));
    }

    // =========================================================
    // 23. GET CREDENTIAL SHARES - VIEW ONLY DENIED
    // =========================================================

    @Test
    void getCredentialShares_viewOnlyUser_throwsForbidden() {

        when(credentialRepository.findById(10L))
                .thenReturn(Optional.of(credential));

        when(sharedCredentialRepository
                .findByCredentialIdAndSharedUserId(10L, 2L))
                .thenReturn(Optional.of(sharedCredential));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> sharingService.getCredentialShares(
                                10L,
                                2L
                        )
                );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    // =========================================================
    // 24. REMOVE ACCESS BY SHARE ID
    // =========================================================

    @Test
    void removeSharedAccessByShareId_owner_success() {

        sharedCredential.setOwner(owner);

        when(sharedCredentialRepository.findById(100L))
                .thenReturn(Optional.of(sharedCredential));

        sharingService.removeSharedAccessByShareId(100L, 1L);

        verify(sharedCredentialRepository)
                .delete(sharedCredential);
    }

    // =========================================================
    // 25. REMOVE ACCESS BY SHARE ID - WRONG OWNER
    // =========================================================

    @Test
    void removeSharedAccessByShareId_wrongOwner_throwsForbidden() {

        sharedCredential.setOwner(owner);

        when(sharedCredentialRepository.findById(100L))
                .thenReturn(Optional.of(sharedCredential));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> sharingService.removeSharedAccessByShareId(
                                100L,
                                999L
                        )
                );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(sharedCredentialRepository, never())
                .delete(any());
    }
}