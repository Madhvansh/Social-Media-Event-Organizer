package com.eventorganizer.utils;

import java.text.Normalizer;
import java.util.Locale;

/**
 * NFKC-based normalization for identifiers (usernames, emails) used as lookup keys.
 * NFKC collapses compatibility characters so "ﬁle" and "file" don't register as
 * distinct identifiers; lowercasing with {@link Locale#ROOT} avoids locale-specific
 * edge cases like Turkish {@code İ}.
 */
public final class Normalize {
    private Normalize() {}

    /**
     * Canonical form for identifier lookups: NFKC + trim + lower-ROOT.
     * Returns {@code null} for null input so callers keep their existing null checks.
     */
    public static String identifier(String s) {
        if (s == null) return null;
        return Normalizer.normalize(s.trim(), Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    }

    /**
     * Returns {@code true} iff the input contains compatibility/non-ASCII characters
     * whose NFKC form differs from a raw ASCII lowercase of the input. Used at
     * registration to reject homoglyph-bearing usernames (e.g., Cyrillic "а" vs ASCII "a").
     */
    public static boolean containsAmbiguousCharacters(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 0x7F) return true;
        }
        return false;
    }
}
