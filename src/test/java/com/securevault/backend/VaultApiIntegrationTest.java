package com.securevault.backend;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.securevault.backend.entity.Credential;
import com.securevault.backend.entity.SharedCredential;
import com.securevault.backend.entity.User;
import com.securevault.backend.repository.CredentialRepository;
import com.securevault.backend.repository.SecurityEventRepository;
import com.securevault.backend.repository.SharedCredentialRepository;
import com.securevault.backend.repository.UserRepository;
import com.securevault.backend.repository.DeviceRepository;
import com.securevault.backend.repository.UserSessionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VaultApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private SharedCredentialRepository sharedCredentialRepository;

    @Autowired
    private SecurityEventRepository securityEventRepository;

    @Autowired
private DeviceRepository deviceRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;


    // =========================================================
    // CLEAN DATABASE BEFORE EACH TEST
    // =========================================================

    @BeforeEach
    void cleanDatabase() {

        sharedCredentialRepository.deleteAll();
        credentialRepository.deleteAll();
        securityEventRepository.deleteAll();
        deviceRepository.deleteAll();
        userSessionRepository.deleteAll();
        userRepository.deleteAll();
    }


    // =========================================================
    // HELPER - REGISTER USER
    // =========================================================

    private void registerUser(
            String username,
            String email,
            String password
    ) throws Exception {

        String request = """
                {
                    "username": "%s",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(username, email, password);

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isCreated());
    }


    // =========================================================
    // HELPER - LOGIN USER
    // =========================================================

    private String loginUser(
            String email,
            String password
    ) throws Exception {

        String request = """
                {
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(email, password);

        String response =
                mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json =
                objectMapper.readTree(response);

        return json.get("token").asText();
    }


    // =========================================================
    // HELPER - CREATE CREDENTIAL
    // =========================================================

    private long createCredential(
            String token,
            String title,
            String username,
            String password
    ) throws Exception {

        String request = """
                {
                    "title": "%s",
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(title, username, password);

        String response =
                mockMvc.perform(
                        post("/api/vault/credentials")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json =
                objectMapper.readTree(response);

        return json.get("id").asLong();
    }


    // =========================================================
    // 1. REGISTER - SUCCESS
    // =========================================================

    @Test
    void registerSuccess() throws Exception {

        String request = """
                {
                    "username": "testuser",
                    "email": "test@example.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.message")
                .value("User registered successfully"))
        .andExpect(jsonPath("$.username")
                .value("testuser"))
        .andExpect(jsonPath("$.email")
                .value("test@example.com"));

        // Verify database change
        org.junit.jupiter.api.Assertions.assertTrue(
                userRepository
                        .findByEmail("test@example.com")
                        .isPresent()
        );
    }


    // =========================================================
    // 2. REGISTER - INVALID INPUT
    // =========================================================

    @Test
    void registerInvalidInput() throws Exception {

        String request = """
                {
                    "username": "",
                    "email": "wrong-email",
                    "password": "123"
                }
                """;

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isBadRequest());
    }


    // =========================================================
    // 3. REGISTER - DUPLICATE EMAIL
    // =========================================================

    @Test
    void registerDuplicateEmail() throws Exception {

        registerUser(
                "user1",
                "duplicate@example.com",
                "Password123"
        );

        String request = """
                {
                    "username": "user2",
                    "email": "duplicate@example.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isInternalServerError());
    }


    // =========================================================
    // 4. LOGIN - SUCCESS
    // =========================================================

    @Test
    void loginSuccess() throws Exception {

        registerUser(
                "loginuser",
                "login@example.com",
                "Password123"
        );

        String request = """
                {
                    "email": "login@example.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message")
                .value("Login successful"))
        .andExpect(jsonPath("$.token")
                .exists())
        .andExpect(jsonPath("$.tokenType")
                .value("Bearer"));
    }


    // =========================================================
    // 5. LOGIN - WRONG PASSWORD
    // =========================================================

    @Test
    void loginWrongPassword() throws Exception {

        registerUser(
                "wrongpass",
                "wrongpass@example.com",
                "Password123"
        );

        String request = """
                {
                    "email": "wrongpass@example.com",
                    "password": "WrongPassword"
                }
                """;

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isInternalServerError());
    }


    // =========================================================
    // 6. CREATE CREDENTIAL - SUCCESS
    // =========================================================

    @Test
    void createCredentialSuccess() throws Exception {

        registerUser(
                "vaultuser",
                "vault@example.com",
                "Password123"
        );

        String token =
                loginUser(
                        "vault@example.com",
                        "Password123"
                );

        long credentialId =
                createCredential(
                        token,
                        "Gmail",
                        "mygmail",
                        "Secret123"
                );

        org.junit.jupiter.api.Assertions.assertTrue(
                credentialRepository
                        .findById(credentialId)
                        .isPresent()
        );
    }


    // =========================================================
    // 7. CREATE CREDENTIAL - INVALID INPUT
    // =========================================================

    @Test
    void createCredentialInvalidInput() throws Exception {

        registerUser(
                "invalidvault",
                "invalidvault@example.com",
                "Password123"
        );

        String token =
                loginUser(
                        "invalidvault@example.com",
                        "Password123"
                );

        String request = """
                {
                    "title": "",
                    "username": "",
                    "password": "123"
                }
                """;

        mockMvc.perform(
                post("/api/vault/credentials")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isBadRequest());
    }


    // =========================================================
    // 8. CREATE CREDENTIAL - UNAUTHORIZED
    // =========================================================

    @Test
    void createCredentialUnauthorized() throws Exception {

        String request = """
                {
                    "title": "Gmail",
                    "username": "user",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
                post("/api/vault/credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isUnauthorized());
    }


    // =========================================================
    // 9. GET / SEARCH CREDENTIALS - SUCCESS
    // =========================================================

    @Test
    void getCredentialsSuccess() throws Exception {

        registerUser(
                "searchuser",
                "search@example.com",
                "Password123"
        );

        String token =
                loginUser(
                        "search@example.com",
                        "Password123"
                );

        createCredential(
                token,
                "GitHub",
                "githubuser",
                "Github123"
        );

        mockMvc.perform(
                get("/api/vault/credentials")
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].title")
                .value("GitHub"))
        .andExpect(jsonPath("$[0].username")
                .value("githubuser"));
    }


    // =========================================================
    // 10. GET / SEARCH CREDENTIALS - UNAUTHORIZED
    // =========================================================

    @Test
    void getCredentialsUnauthorized() throws Exception {

        mockMvc.perform(
                get("/api/vault/credentials")
        )
        .andExpect(status().isUnauthorized());
    }


    // =========================================================
    // 11. UPDATE CREDENTIAL - SUCCESS
    // =========================================================

    @Test
    void updateCredentialSuccess() throws Exception {

        registerUser(
                "updateuser",
                "update@example.com",
                "Password123"
        );

        String token =
                loginUser(
                        "update@example.com",
                        "Password123"
                );

        long credentialId =
                createCredential(
                        token,
                        "Old Title",
                        "olduser",
                        "OldPass123"
                );

        String request = """
                {
                    "title": "Updated Title",
                    "username": "newuser",
                    "password": "NewPass123"
                }
                """;

        mockMvc.perform(
                put("/api/vault/credentials/" + credentialId)
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title")
                .value("Updated Title"))
        .andExpect(jsonPath("$.username")
                .value("newuser"));

        Credential credential =
                credentialRepository
                        .findById(credentialId)
                        .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                "Updated Title",
                credential.getTitle()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "newuser",
                credential.getUsername()
        );
    }


    // =========================================================
    // 12. UPDATE CREDENTIAL - WRONG USER
    // =========================================================

    @Test
    void updateCredentialUnauthorizedUser() throws Exception {

        registerUser(
                "owner",
                "owner@example.com",
                "Password123"
        );

        registerUser(
                "other",
                "other@example.com",
                "Password123"
        );

        String ownerToken =
                loginUser(
                        "owner@example.com",
                        "Password123"
                );

        String otherToken =
                loginUser(
                        "other@example.com",
                        "Password123"
                );

        long credentialId =
                createCredential(
                        ownerToken,
                        "Owner Credential",
                        "owneruser",
                        "OwnerPass123"
                );

        String request = """
                {
                    "title": "Hacked",
                    "username": "hacker",
                    "password": "Hacked123"
                }
                """;

        mockMvc.perform(
                put("/api/vault/credentials/" + credentialId)
                        .header(
                                "Authorization",
                                "Bearer " + otherToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isForbidden());
    }


    // =========================================================
    // 13. DELETE CREDENTIAL - SUCCESS
    // =========================================================

    @Test
    void deleteCredentialSuccess() throws Exception {

        registerUser(
                "deleteuser",
                "delete@example.com",
                "Password123"
        );

        String token =
                loginUser(
                        "delete@example.com",
                        "Password123"
                );

        long credentialId =
                createCredential(
                        token,
                        "Delete Me",
                        "deleteuser",
                        "Delete123"
                );

        mockMvc.perform(
                delete("/api/vault/credentials/" + credentialId)
                        .header(
                                "Authorization",
                                "Bearer " + token
                        )
        )
        .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertFalse(
                credentialRepository
                        .findById(credentialId)
                        .isPresent()
        );
    }


    // =========================================================
    // 14. DELETE CREDENTIAL - UNAUTHORIZED USER
    // =========================================================

    @Test
    void deleteCredentialUnauthorizedUser() throws Exception {

        registerUser(
                "deleteowner",
                "deleteowner@example.com",
                "Password123"
        );

        registerUser(
                "deleteother",
                "deleteother@example.com",
                "Password123"
        );

        String ownerToken =
                loginUser(
                        "deleteowner@example.com",
                        "Password123"
                );

        String otherToken =
                loginUser(
                        "deleteother@example.com",
                        "Password123"
                );

        long credentialId =
                createCredential(
                        ownerToken,
                        "Protected",
                        "owner",
                        "Protected123"
                );

        mockMvc.perform(
                delete("/api/vault/credentials/" + credentialId)
                        .header(
                                "Authorization",
                                "Bearer " + otherToken
                        )
        )
        .andExpect(status().isForbidden());

        org.junit.jupiter.api.Assertions.assertTrue(
                credentialRepository
                        .findById(credentialId)
                        .isPresent()
        );
    }


    // =========================================================
    // 15. SHARE CREDENTIAL - SUCCESS
    // =========================================================

    @Test
    void shareCredentialSuccess() throws Exception {

        registerUser(
                "shareowner",
                "shareowner@example.com",
                "Password123"
        );

        registerUser(
                "shareuser",
                "shareuser@example.com",
                "Password123"
        );

        String ownerToken =
                loginUser(
                        "shareowner@example.com",
                        "Password123"
                );

        long credentialId =
                createCredential(
                        ownerToken,
                        "Shared Gmail",
                        "shareduser",
                        "Shared123"
                );

        String request = """
                {
                    "sharedUserEmail": "shareuser@example.com",
                    "permissionLevel": "VIEW_ONLY",
                    "expirationDate": null
                }
                """;

        mockMvc.perform(
                post("/api/sharing/credentials/" + credentialId)
                        .header(
                                "Authorization",
                                "Bearer " + ownerToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.credentialId")
                .value(credentialId))
        .andExpect(jsonPath("$.permissionLevel")
                .value("VIEW_ONLY"))
        .andExpect(jsonPath("$.sharingStatus")
                .value("ACTIVE"));

        org.junit.jupiter.api.Assertions.assertTrue(
                sharedCredentialRepository
                        .findByCredentialId(credentialId)
                        .size() == 1
        );
    }


    // =========================================================
    // 16. SHARE CREDENTIAL - UNAUTHORIZED
    // =========================================================

    @Test
    void shareCredentialUnauthorized() throws Exception {

        registerUser(
                "shareowner2",
                "shareowner2@example.com",
                "Password123"
        );

        registerUser(
                "shareother2",
                "shareother2@example.com",
                "Password123"
        );

        String ownerToken =
                loginUser(
                        "shareowner2@example.com",
                        "Password123"
                );

        String otherToken =
                loginUser(
                        "shareother2@example.com",
                        "Password123"
                );

        long credentialId =
                createCredential(
                        ownerToken,
                        "Protected Share",
                        "owner",
                        "Password123"
                );

        String request = """
                {
                    "sharedUserEmail": "shareother2@example.com",
                    "permissionLevel": "VIEW_ONLY",
                    "expirationDate": null
                }
                """;

        mockMvc.perform(
                post("/api/sharing/credentials/" + credentialId)
                        .header(
                                "Authorization",
                                "Bearer " + otherToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        )
        .andExpect(status().isForbidden());

        org.junit.jupiter.api.Assertions.assertEquals(
                0,
                sharedCredentialRepository
                        .findByCredentialId(credentialId)
                        .size()
        );
    }
}