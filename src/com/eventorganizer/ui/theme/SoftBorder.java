package com.eventorganizer.ui.theme;

import javax.swing.border.AbstractBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

/**
 * Rounded rectangular border tuned to the warm palette. Paints the rounded
 * outline and reserves inset padding for the caller's content.
 */
public final class SoftBorder extends AbstractBorder {

    private final int radius;
    private final Color color;
    private final int thickness;
    private final Insets padding;

    private SoftBorder(int radius, Color color, int thickness, Insets padding) {
        this.radius = radius;
        this.color = color;
        this.thickness = thickness;
        this.padding = padding;
    }

    public static SoftBorder of(int radius) {
        return new SoftBorder(radius, Theme.BORDER, 1, new Insets(Spacing.M, Spacing.L, Spacing.M, Spacing.L));
    }

    public static SoftBorder of(int radius, Color color) {
        return new SoftBorder(radius, color, 1, new Insets(Spacing.M, Spacing.L, Spacing.M, Spacing.L));
    }

    public static SoftBorder of(int radius, Color color, int thickness) {
        return new SoftBorder(radius, color, thickness, new Insets(Spacing.M, Spacing.L, Spacing.M, Spacing.L));
    }

    public static SoftBorder of(int radius, Color color, int thickness, Insets padding) {
        return new SoftBorder(radius, color, thickness, padding);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new java.awt.BasicStroke(thickness));
            int inset = thickness / 2;
            g2.drawRoundRect(x + inset, y + inset,
                width - thickness, height - thickness,
                radius, radius);
        } finally {
            g2.dispose();
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
