package com.eventorganizer.ui.theme;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;

/**
 * Fake drop shadows painted as stacked translucent rounded rectangles. Java 2D
 * has no native gaussian blur worth the cost on large surfaces; stacking a few
 * offset translucent shapes is 4x cheaper and looks ~95% as good on dark
 * backgrounds.
 *
 * <p>Usage: call {@link #paint(Graphics2D, int, int, int, int, int, Tier)} before
 * painting the surface itself. The shadow is drawn outside the given rect, so
 * components need to reserve padding equal to {@link Tier#outset()} via their
 * border insets if they want the shadow visible within their bounds.
 */
public final class Elevation {
    private Elevation() {}

    public enum Tier {
        E0(new Layer[0]),
        E1(new Layer[] {
            new Layer(0,  2, 6,  40),
        }),
        E2(new Layer[] {
            new Layer(0,  4, 10, 60),
            new Layer(0, 12, 24, 30),
        }),
        E3(new Layer[] {
            new Layer(0,  6, 16, 80),
            new Layer(0, 16, 36, 40),
            new Layer(0, 32, 60, 20),
        });

        private final Layer[] layers;
        Tier(Layer[] layers) { this.layers = layers; }

        public int outset() {
            int max = 0;
            for (Layer l : layers) {
                int reach = Math.abs(l.dy) + l.spread;
                if (reach > max) max = reach;
            }
            return max;
        }
    }

    /** Accent-coloured halo used for focus rings and active nav. */
    public static void paintGlow(Graphics2D g, Shape shape, Color color, float alpha) {
        if (Motion.REDUCED) alpha = Math.min(alpha, 0.25f);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int[] widths = { 8, 5, 2 };
            float[] alphas = { alpha * 0.35f, alpha * 0.55f, alpha };
            for (int i = 0; i < widths.length; i++) {
                g2.setStroke(new java.awt.BasicStroke(widths[i],
                    java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(),
                    Math.min(255, Math.round(alphas[i] * 255f))));
                g2.draw(shape);
            }
        } finally {
            g2.dispose();
        }
    }

    public static void paint(Graphics2D g, int x, int y, int w, int h, int radius, Tier tier) {
        if (tier == Tier.E0 || Motion.REDUCED && tier.ordinal() > Tier.E1.ordinal()) {
            if (tier == Tier.E0) return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (Layer layer : tier.layers) {
                g2.setColor(new Color(0, 0, 0, layer.alpha));
                int lx = x + layer.dx - layer.spread / 2;
                int ly = y + layer.dy - layer.spread / 2;
                int lw = w + layer.spread;
                int lh = h + layer.spread;
                g2.fill(new RoundRectangle2D.Float(lx, ly, lw, lh,
                    radius + layer.spread / 2, radius + layer.spread / 2));
            }
        } finally {
            g2.dispose();
        }
    }

    /** Convenience: paint a shadow for the given rounded shape in user coords. */
    public static void paint(Graphics2D g, RoundRectangle2D shape, Tier tier) {
        paint(g,
            (int) shape.getX(), (int) shape.getY(),
            (int) shape.getWidth(), (int) shape.getHeight(),
            (int) (shape.getArcWidth() / 2.0), tier);
    }

    /** Scale helper for custom components at HiDPI. */
    @SuppressWarnings("unused")
    private static double scaleX(Graphics2D g) {
        AffineTransform t = g.getTransform();
        return t.getScaleX();
    }

    private static final class Layer {
        final int dx, dy, spread, alpha;
        Layer(int dx, int dy, int spread, int alpha) {
            this.dx = dx; this.dy = dy; this.spread = spread; this.alpha = alpha;
        }
    }
}
