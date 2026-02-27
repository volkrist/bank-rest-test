package com.example.bankcards.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Шифрование номера карты и формирование маски **** **** **** 1234.
 */
public final class CardNumberUtil {

    private static final String ALG = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    private CardNumberUtil() {
    }

    /**
     * Маска для отдачи наружу: **** **** **** {last4}
     */
    public static String mask(String lastFour) {
        if (lastFour == null || lastFour.length() != 4) {
            return "**** **** **** ****";
        }
        return "**** **** **** " + lastFour;
    }

    /**
     * Из полного номера (только цифры) извлечь последние 4 цифры.
     */
    public static String lastFour(String fullNumber) {
        if (fullNumber == null) return "";
        String digits = fullNumber.replaceAll("\\D", "");
        return digits.length() >= 4 ? digits.substring(digits.length() - 4) : digits;
    }

    /**
     * Зашифровать номер карты (UTF-8). IV хранится в начале результата (base64).
     */
    public static String encrypt(String plainNumber, byte[] key) {
        if (plainNumber == null || key == null || key.length < 32) {
            throw new IllegalArgumentException("plainNumber and 32-byte key required");
        }
        try {
            SecretKeySpec spec = new SecretKeySpec(key, "AES");
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALG);
            cipher.init(Cipher.ENCRYPT_MODE, spec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plainNumber.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Card number encryption failed", e);
        }
    }

    /**
     * Расшифровать номер карты (для внутренних проверок; наружу не отдавать).
     */
    public static String decrypt(String encryptedBase64, byte[] key) {
        if (encryptedBase64 == null || key == null || key.length < 32) {
            throw new IllegalArgumentException("encrypted and 32-byte key required");
        }
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);
            if (combined.length <= GCM_IV_LENGTH) throw new IllegalArgumentException("Invalid encrypted data");
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);
            SecretKeySpec spec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance(ALG);
            cipher.init(Cipher.DECRYPT_MODE, spec, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Card number decryption failed", e);
        }
    }
}
