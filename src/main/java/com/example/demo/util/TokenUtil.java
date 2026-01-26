package com.example.demo.util;

import java.security.SecureRandom;
import java.util.Base64;

public class TokenUtil {
    private static final SecureRandom SR;
    static {
        try {
            SR = SecureRandom.getInstanceStrong();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String newToken() {
        byte[] b = new byte[48];
        SR.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}
