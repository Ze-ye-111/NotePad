package com.example.notepad;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PrivatePasswordManager {
    private static final String PREFS_NAME = "private_notebook_security";
    private static final String KEY_SALT = "password_salt";
    private static final String KEY_HASH = "password_hash";
    private static final String KEY_ITERATIONS = "password_iterations";
    private static final String KEY_ALGORITHM = "password_algorithm";
    private static final String ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;

    private PrivatePasswordManager() {
    }

    public static boolean hasPassword(Context context) {
        SharedPreferences preferences = preferences(context);
        return !preferences.getString(KEY_SALT, "").isEmpty()
                && !preferences.getString(KEY_HASH, "").isEmpty();
    }

    public static void setPassword(Context context, String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = derive(password, salt, ITERATIONS, ALGORITHM);
        preferences(context).edit()
                .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
                .putString(KEY_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
                .putInt(KEY_ITERATIONS, ITERATIONS)
                .putString(KEY_ALGORITHM, ALGORITHM)
                .apply();
    }

    public static boolean verify(Context context, String password) {
        SharedPreferences preferences = preferences(context);
        String saltValue = preferences.getString(KEY_SALT, "");
        String hashValue = preferences.getString(KEY_HASH, "");
        if (saltValue.isEmpty() || hashValue.isEmpty()) {
            return false;
        }
        try {
            byte[] salt = Base64.decode(saltValue, Base64.NO_WRAP);
            byte[] expected = Base64.decode(hashValue, Base64.NO_WRAP);
            int iterations = preferences.getInt(KEY_ITERATIONS, ITERATIONS);
            String algorithm = preferences.getString(
                    KEY_ALGORITHM, "PBKDF2WithHmacSHA256");
            byte[] actual = derive(password, salt, iterations, algorithm);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations,
                                 String algorithm) {
        char[] passwordChars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(
                passwordChars, salt, iterations, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance(algorithm)
                    .generateSecret(spec)
                    .getEncoded();
        } catch (Exception exception) {
            throw new IllegalStateException("无法初始化私密笔记本密码", exception);
        } finally {
            spec.clearPassword();
            Arrays.fill(passwordChars, '\0');
        }
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
