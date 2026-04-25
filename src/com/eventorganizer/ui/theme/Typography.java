package com.eventorganizer.ui.theme;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;

/**
 * The 12-token type scale. All sizes go through {@link #scale(int)} so a global
 * multiplier (dynamic-type hook) flows everywhere.
 *
 * <p>Java {@link Font} ignores tracking / letter-spacing. For tracked uppercase
 * labels use {@link #drawTracked(Graphics2D, String, Font, float, float, float)}
 * which lays the text out through {@link AttributedString} + {@link TextAttribute#TRACKING}.
 */
public final class Typography {
    private Typography() {}

    /** User-preference scalar; 1.0 = default. */
    public static volatile float SCALE = 1.0f;

    public static int scale(int pt) {
        return Math.max(9, Math.round(pt * SCALE));
    }

    // ---------- Display / headings ----------
    public static final Font DISPLAY_XL = font(Theme.SANS, Font.BOLD, scale(48));
    public static final Font DISPLAY    = font(Theme.SANS, Font.BOLD, scale(32));
    public static final Font H1         = font(Theme.SANS, Font.BOLD, scale(22));
    public static final Font H2         = font(Theme.SANS, Font.BOLD, scale(18));
    public static final Font H3         = font(Theme.SANS, Font.BOLD, scale(15));

    // ---------- Body ----------
    public static final Font BODY       = font(Theme.SANS, Font.PLAIN, scale(13));
    public static final Font BODY_BOLD  = font(Theme.SANS, Font.BOLD,  scale(13));

    // ---------- Labels / captions ----------
    public static final Font LABEL      = font(Theme.SANS, Font.BOLD,  scale(12));
    public static final Font SMALL      = font(Theme.SANS, Font.PLAIN, scale(11));

    // ---------- Mono ----------
    public static final Font MONO       = font(Theme.MONO, Font.PLAIN, scale(12));
    public static final Font MONO_BOLD  = font(Theme.MONO, Font.BOLD,  scale(12));

    // ---------- Hero numerals ----------
    public static final Font NUMERAL    = font(Theme.SANS, Font.BOLD, scale(56));

    private static Font font(String family, int style, int size) {
        return new Font(family, style, size);
    }

    /** Returns a Font derived with explicit weight attributes where the platform supports them. */
    public static Font weight(Font base, float textAttributeWeight) {
        Map<TextAttribute, Object> attrs = new HashMap<>();
        attrs.put(TextAttribute.WEIGHT, textAttributeWeight);
        return base.deriveFont(attrs);
    }

    /**
     * Draws {@code text} at (x baseline, y baseline) with the given letter-spacing
     * expressed as a fraction of the em. Swing's standard Graphics2D#drawString
     * doesn't honour TextAttribute.TRACKING on plain Strings, so we build an
     * AttributedString and lay it out through TextLayout.
     */
    public static void drawTracked(Graphics2D g, String text, Font font, float tracking, float x, float y) {
        if (text == null || text.isEmpty()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            AttributedString as = new AttributedString(text);
            as.addAttribute(TextAttribute.FONT, font);
            as.addAttribute(TextAttribute.TRACKING, tracking);
            TextLayout layout = new TextLayout(as.getIterator(),
                g2.getFontRenderContext());
            layout.draw(g2, x, y);
        } finally {
            g2.dispose();
        }
    }

    /** Measures the pixel width of tracked text. */
    public static float trackedWidth(Graphics2D g, String text, Font font, float tracking) {
        if (text == null || text.isEmpty()) return 0f;
        AttributedString as = new AttributedString(text);
        as.addAttribute(TextAttribute.FONT, font);
        as.addAttribute(TextAttribute.TRACKING, tracking);
        TextLayout layout = new TextLayout(as.getIterator(), g.getFontRenderContext());
        return layout.getAdvance();
    }
}
