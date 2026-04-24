package com.eventorganizer.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Password hashing backed by PBKDF2-HMAC-SHA256.
 *
 * Serialized hash format: {@code "pbkdf2$" + iters + "$" + base64(derivedKey)}.
 * The iteration count is embedded so future bumps are detectable at verify time
 * (see {@link #needsRehash(String)}).
 *
 * Legacy salted-SHA-256 hashes (no prefix) are still accepted by {@link #verify};
 * callers that observe {@code needsRehash == true} after a successful verify
 * should re-hash the password using the current scheme (lazy upgrade path).
 */
public final class PasswordHasher {
    private static final String PBKDF2_ALG = "PBKDF2WithHmacSHA256";
    private static final int PBKDF2_ITERATIONS = 150_000;
    private static final int PBKDF2_KEY_BITS = 256;
    private static final String PREFIX = "pbkdf2$";

    private static final SecureRandom RNG = new SecureRandom();

    // Sentinel salt + hash used by burnDummyVerify() to keep login latency indistinguishable
    // between known and unknown usernames. Initialized lazily (to amortize class-load cost)
    // and reused for every subsequent dummy verify. Not a secret — its only role is to force
    // the same PBKDF2 work a real verify would do.
    private static volatile byte[] SENTINEL_SALT;
    private static volatile String SENTINEL_HASH;

    private PasswordHasher() {}

    public static byte[] generateSalt() {
        byte[] salt = new byte[16];
        RNG.nextBytes(salt);
        return salt;
    }

    /** Primary API: hash a char[] password. Does not zero the caller's array. */
    public static String hash(char[] rawPassword, byte[] salt) {
        if (rawPassword == null || salt == null) {
            throw new IllegalArgumentException("password and salt must not be null");
        }
        byte[] dk = null;
        try {
            dk = derive(rawPassword, salt, PBKDF2_ITERATIONS, PBKDF2_KEY_BITS);
            return PREFIX + PBKDF2_ITERATIONS + "$" + Base64.getEncoder().encodeToString(dk);
        } finally {
            if (dk != null) Arrays.fill(dk, (byte) 0);
        }
    }

    /** Legacy overload retained for callers still holding passwords as String. */
    public static String hash(String rawPassword, byte[] salt) {
        if (rawPassword == null) throw new IllegalArgumentException("password must not be null");
        return hash(rawPassword.toCharArray(), salt);
    }

    /** Constant-time verify that handles both PBKDF2 and legacy SHA-256 hashes. */
    public static boolean verify(char[] rawPassword, byte[] salt, String expectedHash) {
        if (rawPassword == null || salt == null || expectedHash == null) return false;
        if (expectedHash.startsWith(PREFIX)) {
            return verifyPbkdf2(rawPassword, salt, expectedHash);
        }
        return verifyLegacySha256(rawPassword, salt, expectedHash);
    }

    public static boolean verify(String rawPassword, byte[] salt, String expectedHash) {
        if (rawPassword == null) return false;
        return verify(rawPassword.toCharArray(), salt, expectedHash);
    }

    /** True when the stored hash was produced with a weaker scheme or fewer iterations. */
    public static boolean needsRehash(String storedHash) {
        if (storedHash == null) return true;
        if (!storedHash.startsWith(PREFIX)) return true;
        int firstDollar = storedHash.indexOf('$');
        int secondDollar = storedHash.indexOf('$', firstDollar + 1);
        if (secondDollar < 0) return true;
        try {
            int iters = Integer.parseInt(storedHash.substring(firstDollar + 1, secondDollar));
            return iters < PBKDF2_ITERATIONS;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /** Constant-time byte-for-byte equality of two digests (wraps MessageDigest.isEqual). */
    public static boolean constantTimeEquals(byte[] a, byte[] b) {
        return MessageDigest.isEqual(a, b);
    }

    public static void zero(char[] pw) {
        if (pw != null) Arrays.fill(pw, '\0');
    }

    /**
     * Runs a dummy PBKDF2 verify against a sentinel salt+hash. Used by login paths that
     * hit a non-existent user, so the wall-clock cost is indistinguishable from a real
     * password verify. The result is always discarded; caller must still throw an
     * authentication exception regardless.
     */
    public static void burnDummyVerify(char[] rawPassword) {
        byte[] salt = SENTINEL_SALT;
        String hash = SENTINEL_HASH;
        if (salt == null || hash == null) {
            synchronized (PasswordHasher.class) {
                if (SENTINEL_SALT == null || SENTINEL_HASH == null) {
                    SENTINEL_SALT = generateSalt();
                    SENTINEL_HASH = hash(new char[]{'s','e','n','t','i','n','e','l','0'}, SENTINEL_SALT);
                }
                salt = SENTINEL_SALT;
                hash = SENTINEL_HASH;
            }
        }
        // Discard result — timing is the only thing that matters here.
        char[] pw = rawPassword == null ? new char[0] : rawPassword;
        verifyPbkdf2(pw, salt, hash);
    }

    // --- internals ---

    private static boolean verifyPbkdf2(char[] rawPassword, byte[] salt, String expectedHash) {
        int firstDollar = expectedHash.indexOf('$');
        int secondDollar = expectedHash.indexOf('$', firstDollar + 1);
        if (secondDollar < 0) return false;
        int iters;
        byte[] expectedDk;
        try {
            iters = Integer.parseInt(expectedHash.substring(firstDollar + 1, secondDollar));
            expectedDk = Base64.getDecoder().decode(expectedHash.substring(secondDollar + 1));
        } catch (RuntimeException e) {
            return false;
        }
        byte[] actualDk = null;
        try {
            actualDk = derive(rawPassword, salt, iters, expectedDk.length * 8);
            return MessageDigest.isEqual(actualDk, expectedDk);
        } finally {
            if (actualDk != null) Arrays.fill(actualDk, (byte) 0);
            Arrays.fill(expectedDk, (byte) 0);
        }
    }

    private static boolean verifyLegacySha256(char[] rawPassword, byte[] salt, String expectedHash) {
        byte[] utf8 = null;
        try {
            utf8 = toUtf8Bytes(rawPassword);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] actual = md.digest(utf8);
            String actualB64 = Base64.getEncoder().encodeToString(actual);
            byte[] a = actualB64.getBytes(StandardCharsets.US_ASCII);
            byte[] e = expectedHash.getBytes(StandardCharsets.US_ASCII);
            boolean match = MessageDigest.isEqual(a, e);
            Arrays.fill(a, (byte) 0);
            Arrays.fill(e, (byte) 0);
            Arrays.fill(actual, (byte) 0);
            return match;
        } catch (java.security.NoSuchAlgorithmException e) {
            return false;
        } finally {
            if (utf8 != null) Arrays.fill(utf8, (byte) 0);
        }
    }

    private static byte[] derive(char[] rawPassword, byte[] salt, int iters, int keyBits) {
        PBEKeySpec spec = null;
        try {
            spec = new PBEKeySpec(rawPassword, salt, iters, keyBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALG);
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("PBKDF2 derivation failed", e);
        } finally {
            if (spec != null) spec.clearPassword();
        }
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
