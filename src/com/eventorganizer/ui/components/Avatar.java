package com.eventorganizer.ui.components;

import com.eventorganizer.ui.theme.Theme;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Circular initials-avatar. Size presets: 24 / 32 / 48 / 96 px.
 *
 * <p>Background is a stable seeded blend between {@link Theme#BG_OVERLAY} and
 * {@link Theme#ACCENT} so different usernames land on slightly different
 * bronze-tinted backgrounds; the accent ring is drawn 1 px outside the fill.
 */
public final class Avatar extends JComponent {

    public enum Size {
        S24(24, 11), S32(32, 13), S48(48, 18), S96(96, 36);
        final int px;
        final int fontPt;
        Size(int px, int fontPt) { this.px = px; this.fontPt = fontPt; }
    }

    private final String initials;
    private final Size size;
    private final Color bg;
    private final Color ring;

    public Avatar(String displayName, Size size) {
        this.size = size;
        this.initials = initialsOf(displayName);
        int seed = hash(displayName);
        this.bg = seededBg(seed);
        this.ring = Theme.ACCENT;
        Dimension d = new Dimension(size.px, size.px);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        setOpaque(false);
    }

    private static int hash(String s) {
        if (s == null) return 0;
        int h = 0;
        for (int i = 0; i < s.length(); i++) h = h * 31 + s.charAt(i);
        return h & 0x7FFFFFFF;
    }

    private static Color seededBg(int seed) {
        // Blend BG_OVERLAY with a slightly-shifted accent hue, so avatars feel
        // differentiated but still live inside the palette.
        float mix = 0.18f + ((seed % 37) / 37f) * 0.14f;
        int r = Math.round(Theme.BG_OVERLAY.getRed()   * (1 - mix) + Theme.ACCENT.getRed()   * mix);
        int g = Math.round(Theme.BG_OVERLAY.getGreen() * (1 - mix) + Theme.ACCENT.getGreen() * mix);
        int b = Math.round(Theme.BG_OVERLAY.getBlue()  * (1 - mix) + Theme.ACCENT.getBlue()  * mix);
        return new Color(r, g, b);
    }

    private static String initialsOf(String s) {
        if (s == null || s.isEmpty()) return "?";
        String t = s.trim();
        if (t.isEmpty()) return "?";
        String[] parts = t.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            if (!parts[i].isEmpty()) sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.length() == 0 ? t.substring(0, 1).toUpperCase() : sb.toString();
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth(), h = getHeight();
        int d = Math.min(w, h);
        int x = (w - d) / 2, y = (h - d) / 2;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
            g2.setColor(bg);
            g2.fillOval(x, y, d, d);
            g2.setColor(ring);
            g2.setStroke(new BasicStroke(1f));
            g2.drawOval(x, y, d - 1, d - 1);

            g2.setFont(new Font(Theme.SANS, Font.BOLD, size.fontPt));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(initials);
            int tx = x + (d - tw) / 2;
            int ty = y + (d - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.drawString(initials, tx, ty);
        } finally {
            g2.dispose();
        }
    }
}
