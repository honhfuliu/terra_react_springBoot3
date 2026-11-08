package com.ziheng.ai.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AI API Key 加密器
 */
@Component
public class AiApiKeyEncryptor {
    // AES/GCM/NoPadding
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    // IV 长度
    private static final int IV_LENGTH = 12;
    // 标签长度
    private static final int TAG_LENGTH = 128;
    // 密钥
    private final SecretKey secretKey;

    /**
     * 构造函数
     * @param encryptionKey 加密密钥
     */
    public AiApiKeyEncryptor(@Value("${terra.ai.security.encryption-key}") String encryptionKey) {

        byte[] keyBytes = Base64.getDecoder().decode(encryptionKey);

        if (keyBytes.length != 32) {
            throw new IllegalArgumentException("AES密钥必须是32字节");
        }

        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * 加密
     */
    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.isBlank()) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));

            byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            // IV + 密文，一起进行 Base64 编码
            byte[] result = ByteBuffer.allocate(iv.length + encrypted.length)
                    .put(iv)
                    .put(encrypted)
                    .array();
            return Base64.getEncoder().encodeToString(result);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("API Key 加密失败", e);
        }
    }

    /**
     * 解密
     */
    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.isBlank()) {
            return null;
        }

        try {
            byte[] encryptedData = Base64.getDecoder().decode(ciphertext);
            ByteBuffer buffer = ByteBuffer.wrap(encryptedData);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("API Key 解密失败", e);
        }
    }
}
