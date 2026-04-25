package com.eventorganizer.utils;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import com.eventorganizer.exceptions.ErrorCode;
import com.eventorganizer.exceptions.ValidationException;
import com.eventorganizer.store.DataStore;

public final class Validator {
    private static final Pattern EMAIL_REGEX =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_REGEX =
        Pattern.compile("^[A-Za-z0-9_]+$");
    private static final Pattern ANSI_ESCAPE =
        Pattern.compile("\\u001B\\[[0-9;]*[a-zA-Z]");

    // Hard upper bound for any regex / loop over user-supplied text.
    // Prevents pathological inputs from being scanned without a length gate (ReDoS / DoS hygiene).
    private static final int MAX_REASONABLE = 10_000;

    private Validator() {}

    public static boolean isValidEmail(String s) {
        if (s == null || s.length() > Limits.EMAIL_MAX) return false;
        return EMAIL_REGEX.matcher(s).matches();
    }

    public static boolean isValidUsername(String s) {
        if (s == null || s.length() > Limits.USERNAME_MAX) return false;
        return USERNAME_REGEX.matcher(s).matches();
    }

    public static boolean isNonEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static boolean isFutureDate(LocalDateTime dt) {
        return dt != null && dt.isAfter(LocalDateTime.now(DataStore.getClockOrDefault()));
    }

    public static boolean hasNoControlChars(String s) {
        if (s == null) return true;
        if (s.length() > MAX_REASONABLE) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 0x20 || c == 0x7F || c == '​' || c == '﻿') return false;
        }
        return true;
    }

    public static String stripAnsi(String s) {
        if (s == null) return null;
        String bounded = s.length() > MAX_REASONABLE ? s.substring(0, MAX_REASONABLE) : s;
        return ANSI_ESCAPE.matcher(bounded).replaceAll("");
    }

    public static boolean isPasswordStrong(char[] pw) {
        if (pw == null || pw.length < Limits.PASSWORD_MIN || pw.length > Limits.PASSWORD_MAX) return false;
        boolean hasLetter = false, hasDigit = false, onlyWhitespace = true;
        for (char c : pw) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c))  hasDigit = true;
            if (!Character.isWhitespace(c)) onlyWhitespace = false;
        }
        return hasLetter && hasDigit && !onlyWhitespace;
    }

    /**
     * Reject inputs exceeding {@code max} chars before any further validation.
     * Returns the input unchanged on success; throws {@link ValidationException} otherwise.
     * Null is treated as valid (let upstream null-checks produce a better message).
     */
    public static String requireLength(String s, int max, String fieldName) {
        if (s == null) return null;
        if (s.length() > max) {
            throw new ValidationException(
                fieldName + " must be at most " + max + " characters.",
                ErrorCode.ERR_VALIDATION);
        }
        return s;
    }

    /**
     * Reject null or blank inputs without leaking the supplied value back in the error message.
     * Used at service entry points where the caller must provide a non-empty identifier.
     */
    public static String requireNonBlank(String s, String fieldName) {
        if (s == null || s.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required.", ErrorCode.ERR_VALIDATION);
        }
        return s;
    }
}
