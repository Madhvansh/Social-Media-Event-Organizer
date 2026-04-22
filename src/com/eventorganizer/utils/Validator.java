package com.eventorganizer.utils;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import com.eventorganizer.store.DataStore;

public final class Validator {
    private static final Pattern EMAIL_REGEX =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_REGEX =
        Pattern.compile("^[A-Za-z0-9_]+$");
    private static final Pattern ANSI_ESCAPE =
        Pattern.compile("\\u001B\\[[0-9;]*[a-zA-Z]");

    private Validator() {}

    public static boolean isValidEmail(String s) {
        return s != null && EMAIL_REGEX.matcher(s).matches();
    }

    public static boolean isValidUsername(String s) {
        return s != null && USERNAME_REGEX.matcher(s).matches();
    }

    public static boolean isNonEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static boolean isFutureDate(LocalDateTime dt) {
        return dt != null && dt.isAfter(LocalDateTime.now(DataStore.getClockOrDefault()));
    }

    public static boolean hasNoControlChars(String s) {
        if (s == null) return true;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 0x20 || c == 0x7F || c == '\u200B' || c == '\uFEFF') return false;
        }
        return true;
    }

    public static String stripAnsi(String s) {
        return s == null ? null : ANSI_ESCAPE.matcher(s).replaceAll("");
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
}
