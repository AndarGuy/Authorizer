package me.andarguy.authorizer.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.nio.charset.StandardCharsets;

public class CryptoUtils {
    private static final BCrypt.Verifyer HASH_VERIFIER = BCrypt.verifyer();
    private static final BCrypt.Hasher HASHER = BCrypt.withDefaults();

    public static boolean checkPassword(String password, String hash) {
        return HASH_VERIFIER.verify(
                password.getBytes(StandardCharsets.UTF_8),
                hash.replace("BCRYPT$", "$2a$").getBytes(StandardCharsets.UTF_8)
        ).verified;
    }

    public static String generateHash(String password) {
        return HASHER.hashToString(10, password.toCharArray());
    }
}
