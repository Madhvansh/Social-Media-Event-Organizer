package com.eventorganizer.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public final class PasswordHasher {
    private static final SecureRandom RNG = new SecureRandom();

    private PasswordHasher() {}

    public static byte[] generateSalt() {
        byte[] salt = new byte[16];
        RNG.nextBytes(salt);
        return salt;
    }

    /** Primary API: hash a char[] password. Does not zero the input. Caller owns lifetime. */
    public static String hash(char[] rawPassword, byte[] salt) {
        if (rawPassword == null || salt == null) {
            throw new IllegalArgumentException("password and salt must not be null");
        }
        byte[] utf8 = null;
        try {
            utf8 = toUtf8Bytes(rawPassword);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashed = md.digest(utf8);
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        } finally {
            if (utf8 != null) Arrays.fill(utf8, (byte) 0);
        }
    }

    /** Overload kept for those using password as String. */
    public static String hash(String rawPassword, byte[] salt) {
        if (rawPassword == null) throw new IllegalArgumentException("password must not be null");
        return hash(rawPassword.toCharArray(), salt);
    }

    /** Constant-time verification; prefers char[] to prevent interning raw password. */
    public static boolean verify(char[] rawPassword, byte[] salt, String expectedHash) {
        if (rawPassword == null || salt == null || expectedHash == null) return false;
        String actual = hash(rawPassword, salt);
        byte[] a = actual.getBytes(StandardCharsets.US_ASCII);
        byte[] e = expectedHash.getBytes(StandardCharsets.US_ASCII);
        boolean match = MessageDigest.isEqual(a, e);
        Arrays.fill(a, (byte) 0);
        Arrays.fill(e, (byte) 0);
        return match;
    }

    public static boolean verify(String rawPassword, byte[] salt, String expectedHash) {
        if (rawPassword == null) return false;
        return verify(rawPassword.toCharArray(), salt, expectedHash);
    }

    /** Constant time byte-for-byte equality of 2 digests. */
    public static boolean constantTimeEquals(byte[] a, byte[] b) {
        return MessageDigest.isEqual(a, b);
    }

    public static void zero(char[] pw) {
        if (pw != null) Arrays.fill(pw, '\0');
    }

    private static byte[] toUtf8Bytes(char[] chars) {
        CharBuffer cb = CharBuffer.wrap(chars);
        ByteBuffer bb = StandardCharsets.UTF_8.encode(cb);
        byte[] out = new byte[bb.remaining()];
        bb.get(out);
        if (bb.hasArray()) Arrays.fill(bb.array(), (byte) 0);
        return out;
    }
}
