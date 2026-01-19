package com.lax.sme_manager.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for security operations (PIN hashing)
 */
public class SecurityUtil {

    /**
     * Hash a PIN using SHA-256
     * @param pin The plain text PIN
     * @return SHA-256 hashed PIN encoded in Base64
     */
    public static String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Verify a PIN against its hash
     * @param pin Plain text PIN to verify
     * @param hash Stored hash to compare against
     * @return true if PIN matches hash, false otherwise
     */
    public static boolean verifyPin(String pin, String hash) {
        return hashPin(pin).equals(hash);
    }
}
