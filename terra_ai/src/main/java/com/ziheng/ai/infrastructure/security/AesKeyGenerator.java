package com.ziheng.ai.infrastructure.security;

import javax.crypto.KeyGenerator;
import java.util.Base64;

/**
 * AES key generator
 */
public class AesKeyGenerator {

    public static void main(String[] args) throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256);

        byte[] key = generator.generateKey().getEncoded();

        System.out.println(Base64.getEncoder().encodeToString(key));
    }
}
