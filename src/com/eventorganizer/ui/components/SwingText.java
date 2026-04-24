package com.eventorganizer.ui.components;

/**
 * Swing labels auto-parse HTML when their text starts with {@code <html>}. If a
 * user-controlled field (bio, event name, etc.) happens to begin with that token,
 * the label will render tags as markup — small risk, but easy to eliminate.
 *
 * {@link #plain(String)} returns a string safe to drop into any {@code JLabel},
 * {@code JButton}, or {@code JMenuItem} without worrying about HTML interpretation.
 * The trick: prefix any input that starts with {@code <} with a zero-width
 * no-break space, which stops Swing's HTML detector (it only triggers on a
 * leading {@code <html>} token) without being visually noticeable.
 */
public final class SwingText {
    private static final char ZWNBSP = '﻿';

    private SwingText() {}

    /** Returns {@code s} made safe for Swing label/button text rendering. */
    public static String plain(String s) {
        if (s == null) return "";
        if (s.isEmpty()) return s;
        if (s.charAt(0) == '<') return ZWNBSP + s;
        return s;
    }
}
