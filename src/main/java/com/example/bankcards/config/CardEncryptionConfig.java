package com.example.bankcards.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
public class CardEncryptionConfig {

    @Value("${card.encryption-key:${CARD_ENCRYPTION_KEY:}}")
    private String encryptionKeyBase64;

    @Bean
    public byte[] cardEncryptionKey() {
        if (encryptionKeyBase64 == null || encryptionKeyBase64.isBlank()) {
            throw new IllegalStateException(
                    "card.encryption-key (or CARD_ENCRYPTION_KEY) must be set; no default for security");
        }
        byte[] key = Base64.getDecoder().decode(encryptionKeyBase64.trim());
        if (key.length != 32) {
            throw new IllegalStateException("card.encryption-key must be base64 of 32 bytes (AES-256)");
        }
        return key;
    }
}
