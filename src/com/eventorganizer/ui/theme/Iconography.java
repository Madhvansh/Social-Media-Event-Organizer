package com.eventorganizer.ui.theme;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Stroke-only monoline glyph registry — all shapes are built on a 24×24 design
 * grid and re-scaled via {@link #paint(Graphics2D, String, float, float, float, Color)}.
 * No external assets; zero dependencies; crisp at every DPI.
 *
 * <p>The registry is append-only. To add a glyph, push a lambda that returns a
 * {@link Path2D.Float} on the 24×24 grid. Painting applies an affine transform
 * + stroke of width {@code size / 18f}, which keeps the visual weight uniform
 * across sizes.
 */
public final class Iconography {
    private Iconography() {}

    private static final int GRID = 24;
    private static final Map<String, Function<Void, Shape>> REG = new HashMap<>();

    static {
        reg("calendar", v -> {
            Path2D.Float p = new Path2D.Float();
            p.append(new RoundRectangle2D.Float(3, 5, 18, 16, 2, 2), false);
            p.moveTo(3, 10); p.lineTo(21, 10);
            p.moveTo(8, 3);  p.lineTo(8, 7);
            p.moveTo(16, 3); p.lineTo(16, 7);
            return p;
        });
        reg("check", v -> {
            Path2D.Float p = new Path2D.Float();
            p.moveTo(5, 12); p.lineTo(10, 17); p.lineTo(19, 7);
            return p;
        });
        reg("x", v -> {
            Path2D.Float p = new Path2D.Float();
            p.moveTo(6, 6);  p.lineTo(18, 18);
            p.moveTo(18, 6); p.lineTo(6, 18);
            return p;
        });
        reg("plus", v -> {
            Path2D.Float p = new Path2D.Float();
            p.moveTo(12, 5); p.lineTo(12, 19);
            p.moveTo(5, 12); p.lineTo(19, 12);
            return p;
        });
        reg("search", v -> {
            Path2D.Float p = new Path2D.Float();
            p.append(new Ellipse2D.Float(4, 4, 13, 13), false);
            p.moveTo(15, 15); p.lineTo(20, 20);
            return p;
        });
        reg("bell", v -> {
            Path2D.Float p = new Path2D.Float();
            p.moveTo(6, 17);
            p.curveTo(6, 14, 6, 11, 6, 10);
            p.curveTo(6, 6.5f, 8.5f, 4, 12, 4);
            p.curveTo(15.5f, 4, 18, 6.5f, 18, 10);
            p.curveTo(18, 11, 18, 14, 18, 17);
            p.lineTo(6, 17);
            p.closePath();
            p.moveTo(10, 20);
            p.curveTo(10.5f, 21.5f, 13.5f, 21.5f, 14, 20);
            return p;
        });
        reg("user", v -> {
            Path2D.Float p = new Path2D.Float();
            p.append(new Ellipse2D.Float(8, 4, 8, 8), false);
            p.moveTo(4, 20); p.curveTo(4, 15, 8, 14, 12, 14);
            p.curveTo(16, 14, 20, 15, 20, 20);
            return p;
        });
        reg("users", v -> {
            Path2D.Float p = new Path2D.Float();
            p.append(new Ellipse2D.Float(6, 5, 6, 6), false);
            p.moveTo(2, 20); p.curveTo(2, 16, 5, 14, 9, 14);
            p.curveTo(13, 14, 16, 16, 16, 20);
            p.append(new Ellipse2D.Float(14, 4, 5, 5), false);
            p.moveTo(17, 14); p.curveTo(20, 14, 22, 16, 22, 20);
            return p;
        });
        reg("home", v -> {
            Path2D.Float p = new Path2D.Float();
            p.moveTo(3, 11); p.lineTo(12, 3); p.lineTo(21, 11);
            p.moveTo(5, 10); p.lineTo(5, 21); p.lineTo(19, 21); p.lineTo(19, 10);
            return p;
        });
        reg("compass", v -> {
            Path2D.Float p = new Path2D.Float();
            p.append(new Ellipse2D.Float(3, 3, 18, 18), false);
            p.moveTo(16, 8);  p.lineTo(13, 13); p.lineTo(8, 16);
            p.lineTo(11, 11); p.closePath();
            return p;
        });
        reg("star", v -> {
            Path2D.Float p = new Path2D.Float();
            float[][] pts = starPts(12, 12, 9.5f, 4f, 5);
            p.moveTo(pts[0][0], pts[0][1]);
            for (int i = 1; i < pts.length; i++) p.lineTo(pts[i][0], pts[i][1]);
            p.closePath();
            return p;
        });
        reg("lock", v -> {
            Path2D.Float p = new Path2D.Float();
            p.append(new RoundRectangle2D.Float(5, 11, 14, 10, 2, 2), false);
            p.moveTo(8, 11); p.lineTo(8, 8);
            p.curveTo(8, 5.5f, 9.8f, 4, 12, 4);
            p.curveTo(14.2f, 4, 16, 5.5f, 16, 8); p.lineTo(16, 11);
            return p;
        });
        reg("eye", v -> {
            Path2D.Float p = new Path2D.Float();
            p.moveTo(2, 12);
            p.curveTo(5, 6, 8.5f, 5, 12, 5);
            p.curveTo(15.5f, 5, 19, 6, 22, 12);
            p.curveTo(19, 18, 15.5f, 19, 12, 19);
            p.curveTo(8.5f, 19, 5, 18, 2, 12);
            p.closePath();
            p.append(new Ellipse2D.Float(9, 9, 6, 6), false);
            return p;
        });
        reg("chevron-right", v -> {
            Path2D.Float p = new Path2D.Float();
            p.moveTo(9, 6); p.lineTo(15, 12); p.lineTo(9, 18);
            return p;
        });
        reg("chevron-down", v -> {
            Path2D.Float p = new Path2D.Float();
            p.moveTo(6, 9); p.lineTo(12, 15); p.lineTo(18, 9);
            return p;
        });
        reg("arrow-back", v -> {
            Path2D.Float p = new Path2D.Float();
            p.moveTo(10, 5); p.lineTo(4, 12); p.lineTo(10, 19);
            p.moveTo(4, 12); p.lineTo(20, 12);
            return p;
        });
        reg("ellipsis", v -> {
            Path2D.Float p = new Path2D.Float();
            p.append(new Ellipse2D.Float(3, 10, 3, 3), false);
            p.append(new Ellipse2D.Float(10.5f, 10, 3, 3), false);
            p.append(new Ellipse2D.Float(18, 10, 3, 3), false);
            return p;
        });
        reg("settings", v -> {
            Path2D.Float p = new Path2D.Float();
            p.append(new Ellipse2D.Float(8, 8, 8, 8), false);
            for (int i = 0; i < 8; i++) {
                double a = i * Math.PI / 4;
                float rx1 = 12 + (float)(Math.cos(a) * 8);
                float ry1 = 12 + (float)(Math.sin(a) * 8);
                float rx2 = 12 + (float)(Math.cos(a) * 11);
                float ry2 = 12 + (float)(Math.sin(a) * 11);
                p.moveTo(rx1, ry1); p.lineTo(rx2, ry2);
            }
            return p;
        });
        reg("sparkle", v -> {
            Path2D.Float p = new Path2D.Float();
            p.moveTo(12, 3);  p.lineTo(13, 11); p.lineTo(21, 12);
            p.lineTo(13, 13); p.lineTo(12, 21); p.lineTo(11, 13);
            p.lineTo(3, 12);  p.lineTo(11, 11); p.closePath();
            return p;
        });
        reg("pin", v -> {
            Path2D.Float p = new Path2D.Float();
            p.moveTo(12, 21);
            p.curveTo(6, 15, 5, 12, 5, 9);
            p.curveTo(5, 5.5f, 8.1f, 3, 12, 3);
            p.curveTo(15.9f, 3, 19, 5.5f, 19, 9);
            p.curveTo(19, 12, 18, 15, 12, 21);
            p.closePath();
            p.append(new Ellipse2D.Float(10, 7, 4, 4), false);
            return p;
        });
        reg("envelope", v -> {
            Path2D.Float p = new Path2D.Float();
            p.append(new RoundRectangle2D.Float(3, 5, 18, 14, 2, 2), false);
            p.moveTo(3, 7); p.lineTo(12, 13); p.lineTo(21, 7);
            return p;
        });
        reg("clock", v -> {
            Path2D.Float p = new Path2D.Float();
            p.append(new Ellipse2D.Float(3, 3, 18, 18), false);
            p.moveTo(12, 7); p.lineTo(12, 12); p.lineTo(16, 14);
            return p;
        });
        reg("logout", v -> {
            Path2D.Float p = new Path2D.Float();
            p.moveTo(10, 4); p.lineTo(4, 4); p.lineTo(4, 20); p.lineTo(10, 20);
            p.moveTo(9, 12); p.lineTo(21, 12);
            p.moveTo(17, 8); p.lineTo(21, 12); p.lineTo(17, 16);
            return p;
        });
        reg("chart", v -> {
            Path2D.Float p = new Path2D.Float();
            p.moveTo(4, 20); p.lineTo(4, 4); p.moveTo(4, 20); p.lineTo(20, 20);
            p.moveTo(8, 16); p.lineTo(8, 12);
            p.moveTo(12, 16); p.lineTo(12, 8);
            p.moveTo(16, 16); p.lineTo(16, 10);
            return p;
        });
        reg("edit", v -> {
            Path2D.Float p = new Path2D.Float();
            p.moveTo(4, 20); p.lineTo(4, 16); p.lineTo(16, 4); p.lineTo(20, 8);
            p.lineTo(8, 20); p.closePath();
            p.moveTo(13, 7); p.lineTo(17, 11);
            return p;
        });
    }

    private static void reg(String name, Function<Void, Shape> f) {
        REG.put(name, f);
    }

    private static float[][] starPts(float cx, float cy, float rOuter, float rInner, int points) {
        float[][] out = new float[points * 2][2];
        double step = Math.PI / points;
        for (int i = 0; i < points * 2; i++) {
            double a = -Math.PI / 2 + i * step;
            float r = (i % 2 == 0) ? rOuter : rInner;
            out[i][0] = cx + (float)(Math.cos(a) * r);
            out[i][1] = cy + (float)(Math.sin(a) * r);
        }
        return out;
    }

    public static boolean has(String name) {
        return REG.containsKey(name);
    }

    /** Paints the named glyph at (x, y) with {@code size × size} bounds in user coords. */
    public static void paint(Graphics2D g, String name, float x, float y, float size, Color color) {
        Function<Void, Shape> f = REG.get(name);
        if (f == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            AffineTransform at = new AffineTransform();
            at.translate(x, y);
            float s = size / GRID;
            at.scale(s, s);
            g2.transform(at);
            float stroke = Math.max(1f, size / 14f);
            g2.setStroke(new BasicStroke(stroke / s,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(color);
            g2.draw(f.apply(null));
        } finally {
            g2.dispose();
        }
    }
}
