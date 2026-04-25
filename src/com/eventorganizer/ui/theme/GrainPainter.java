package com.eventorganizer.ui.theme;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Lazily generates a 256x256 monochrome noise tile and paints it as a
 * repeating TexturePaint at ~5% alpha over flat surfaces. Defeats 8-bit
 * banding on large backgrounds without drawing attention to itself.
 */
public final class GrainPainter {
    private GrainPainter() {}

    private static final int TILE = 256;
    private static final long SEED = 0xD15EA5EL;
    private static volatile TexturePaint PAINT;

    private static TexturePaint paint() {
        TexturePaint p = PAINT;
        if (p != null) return p;
        synchronized (GrainPainter.class) {
            if (PAINT != null) return PAINT;
            BufferedImage tile = new BufferedImage(TILE, TILE, BufferedImage.TYPE_INT_ARGB);
            Random r = new Random(SEED);
            for (int y = 0; y < TILE; y++) {
                for (int x = 0; x < TILE; x++) {
                    int v = r.nextInt(256);
                    int rgb = (12 << 24) | (v << 16) | (v << 8) | v;
                    tile.setRGB(x, y, rgb);
                }
            }
            PAINT = new TexturePaint(tile, new Rectangle(0, 0, TILE, TILE));
            return PAINT;
        }
    }

    public static void paintGrain(Graphics2D g2, int width, int height) {
        paintGrain(g2, width, height, 0.05f);
    }

    public static void paintGrain(Graphics2D g2, int width, int height, float alpha) {
        if (width <= 0 || height <= 0) return;
        java.awt.Composite old = g2.getComposite();
        java.awt.Paint oldPaint = g2.getPaint();
        try {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setPaint(paint());
            g2.fillRect(0, 0, width, height);
        } finally {
            g2.setPaint(oldPaint);
            g2.setComposite(old);
        }
    }

    public static Color tileSampleColor() {
        return new Color(128, 128, 128);
    }
}
