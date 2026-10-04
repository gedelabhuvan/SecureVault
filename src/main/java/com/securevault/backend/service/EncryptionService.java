package com.securevault.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public EncryptionService(
            @Value("${securevault.encryption-key}") String encodedKey
    ) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(encodedKey);

            if (keyBytes.length != 32) {
                throw new IllegalArgumentException(
                        "Encryption key must decode to exactly 32 bytes"
                );
            }

            this.secretKey = new SecretKeySpec(keyBytes, "AES");

        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Invalid SecureVault encryption key configuration",
                    e
            );
        }
    }

    public String encrypt(String plainText) {

        if (plainText == null) {
            return null;
        }

        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            GCMParameterSpec parameterSpec =
                    new GCMParameterSpec(
                            GCM_TAG_LENGTH,
                            iv
                    );

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    parameterSpec
            );

            byte[] encryptedBytes =
                    cipher.doFinal(
                            plainText.getBytes(StandardCharsets.UTF_8)
                    );

            /*
             * Store IV together with ciphertext.
             *
             * Format:
             * Base64(IV) : Base64(Ciphertext + Authentication Tag)
             */
            return Base64.getEncoder().encodeToString(iv)
                    + ":"
                    + Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to encrypt credential data",
                    e
            );
        }
    }

    public String decrypt(String encryptedText) {

        if (encryptedText == null) {
            return null;
        }

        try {
            String[] parts = encryptedText.split(":", 2);

            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        "Invalid encrypted credential format"
                );
            }

            byte[] iv =
                    Base64.getDecoder().decode(parts[0]);

            byte[] encryptedBytes =
                    Base64.getDecoder().decode(parts[1]);

            if (iv.length != IV_LENGTH) {
                throw new IllegalArgumentException(
                        "Invalid encryption IV"
                );
            }

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            GCMParameterSpec parameterSpec =
                    new GCMParameterSpec(
                            GCM_TAG_LENGTH,
                            iv
                    );

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    parameterSpec
            );

            byte[] decryptedBytes =
                    cipher.doFinal(encryptedBytes);

            return new String(
                    decryptedBytes,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to decrypt credential data",
                    e
            );
        }
    }
}