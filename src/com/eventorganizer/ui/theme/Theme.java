package com.eventorganizer.ui.theme;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class Theme {
    private Theme() {}

    public static final Color BG_PRIMARY   = new Color(0x1E1E2E);
    public static final Color BG_ELEVATED  = new Color(0x282838);
    public static final Color BG_HOVER     = new Color(0x313145);
    public static final Color TEXT_PRIMARY = new Color(0xE8E8F0);
    public static final Color TEXT_MUTED   = new Color(0x8A8AA3);
    public static final Color ACCENT       = new Color(0x7B8CFF);
    public static final Color ACCENT_HOVER = new Color(0x95A3FF);
    public static final Color SUCCESS      = new Color(0x55E0A0);
    public static final Color WARNING      = new Color(0xF2C464);
    public static final Color DANGER       = new Color(0xE55770);
    public static final Color BORDER       = new Color(0x3A3A52);

    private static final String PREFERRED_SANS = "Inter";
    private static final String PREFERRED_MONO = "JetBrains Mono";
    private static final String FALLBACK_SANS  = Font.SANS_SERIF;
    private static final String FALLBACK_MONO  = Font.MONOSPACED;

    private static final String SANS = resolveFamily(PREFERRED_SANS, FALLBACK_SANS);
    private static final String MONO = resolveFamily(PREFERRED_MONO, FALLBACK_MONO);

    public static final Font FONT_DISPLAY = new Font(SANS, Font.BOLD,  22);
    public static final Font FONT_TITLE   = new Font(SANS, Font.BOLD,  16);
    public static final Font FONT_BODY    = new Font(SANS, Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font(SANS, Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font(MONO, Font.PLAIN, 12);

    private static String resolveFamily(String preferred, String fallback) {
        try {
            Set<String> available = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            return available.contains(preferred) ? preferred : fallback;
        } catch (RuntimeException e) {
            return fallback;
        }
    }
}
