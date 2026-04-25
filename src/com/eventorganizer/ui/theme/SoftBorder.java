package com.eventorganizer.ui.theme;

import javax.swing.border.AbstractBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Rounded rectangular border tuned to the warm palette. Paints the rounded
 * outline and reserves inset padding for the caller's content.
 *
 * <p>Optional outer glow (2-layer halo) is painted when {@link #withGlow} is
 * supplied — used for focus rings and active nav indicators.
 */
public final class SoftBorder extends AbstractBorder {

    private final int radius;
    private final Color color;
    private final int thickness;
    private final Insets padding;
    private final Color glowColor;
    private final float glowAlpha;

    private SoftBorder(int radius, Color color, int thickness, Insets padding,
                       Color glowColor, float glowAlpha) {
        this.radius = radius;
        this.color = color;
        this.thickness = thickness;
        this.padding = padding;
        this.glowColor = glowColor;
        this.glowAlpha = glowAlpha;
    }

    public static SoftBorder of(int radius) {
        return new SoftBorder(radius, Theme.BORDER, 1,
            new Insets(Spacing.M, Spacing.L, Spacing.M, Spacing.L), null, 0f);
    }

    public static SoftBorder of(int radius, Color color) {
        return new SoftBorder(radius, color, 1,
            new Insets(Spacing.M, Spacing.L, Spacing.M, Spacing.L), null, 0f);
    }

    public static SoftBorder of(int radius, Color color, int thickness) {
        return new SoftBorder(radius, color, thickness,
            new Insets(Spacing.M, Spacing.L, Spacing.M, Spacing.L), null, 0f);
    }

    public static SoftBorder of(int radius, Color color, int thickness, Insets padding) {
        return new SoftBorder(radius, color, thickness, padding, null, 0f);
    }

    /** Returns a copy with an outer accent-colored halo (2-layer). */
    public SoftBorder withGlow(Color glow, float alpha) {
        return new SoftBorder(radius, color, thickness, padding, glow, alpha);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (glowColor != null && glowAlpha > 0f && !Motion.REDUCED) {
                paintGlow(g2, x, y, width, height);
            }

            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            int inset = thickness / 2;
            g2.drawRoundRect(x + inset, y + inset,
                width - thickness, height - thickness,
                radius, radius);
        } finally {
            g2.dispose();
        }
    }

    private void paintGlow(Graphics2D g2, int x, int y, int w, int h) {
        int[] widths = { 8, 5, 2 };
        float[] mults = { 0.30f, 0.55f, 0.85f };
        for (int i = 0; i < widths.length; i++) {
            int a = Math.min(255, Math.round(glowAlpha * mults[i] * 255f));
            g2.setColor(new Color(glowColor.getRed(), glowColor.getGreen(),
                glowColor.getBlue(), a));
            g2.setStroke(new BasicStroke(widths[i], BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f,
                w - 1f, h - 1f, radius, radius));
        }
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(padding.top, padding.left, padding.bottom, padding.right);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.top = padding.top;
        insets.left = padding.left;
        insets.bottom = padding.bottom;
        insets.right = padding.right;
        return insets;
    }
}
