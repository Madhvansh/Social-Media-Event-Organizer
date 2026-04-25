package com.eventorganizer.ui.theme;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;

/**
 * Canonical gradient factories for the Obsidian Aurora system. Each call
 * allocates lightweight Paint objects — safe to invoke per paint.
 */
public final class Gradient {
    private Gradient() {}

    /** Bronze→amber→bronze diagonal sweep for hero borders and default-button fills. */
    public static Paint bronzeSweep(int w, int h) {
        return new LinearGradientPaint(
            new Point2D.Float(0, 0),
            new Point2D.Float(Math.max(1, w), Math.max(1, h)),
            new float[] { 0f, 0.5f, 1f },
            new Color[] { Theme.ACCENT, Theme.ACCENT_HOVER, Theme.ACCENT });
    }

    /** Amethyst sweep for incoming-signal hero elements. */
    public static Paint plumSweep(int w, int h) {
        return new LinearGradientPaint(
            new Point2D.Float(0, 0),
            new Point2D.Float(Math.max(1, w), Math.max(1, h)),
            new float[] { 0f, 1f },
            new Color[] { Theme.ACCENT2, new Color(0x7F6596) });
    }

    /** Subtle top-lit wash for elevated cards. Gives a "lit from above" feel. */
    public static Paint elevatedWash(int w, int h) {
        return new LinearGradientPaint(
            new Point2D.Float(0, 0),
            new Point2D.Float(0, Math.max(1, h)),
            new float[] { 0f, 1f },
            new Color[] { new Color(0x2A231C), Theme.BG_ELEVATED });
    }

    /** Dialog / glass-card fill: softly lit dark pane. */
    public static Paint glassFill(int w, int h) {
        return new LinearGradientPaint(
            new Point2D.Float(0, 0),
            new Point2D.Float(0, Math.max(1, h)),
            new float[] { 0f, 1f },
            new Color[] { new Color(38, 32, 27, 225), new Color(26, 21, 17, 225) });
    }

    /** Single aurora blob. Used four times per frame by MeshBackdrop. */
    public static RadialGradientPaint blob(float cx, float cy, float radius, Color core, int coreAlpha) {
        radius = Math.max(1f, radius);
        Color c0 = new Color(core.getRed(), core.getGreen(), core.getBlue(), coreAlpha);
        Color c1 = new Color(core.getRed(), core.getGreen(), core.getBlue(), coreAlpha / 2);
        Color c2 = new Color(core.getRed(), core.getGreen(), core.getBlue(), 0);
        return new RadialGradientPaint(
            new Point2D.Float(cx, cy),
            radius,
            new float[] { 0f, 0.55f, 1f },
            new Color[] { c0, c1, c2 });
    }

    /** Pre-canned accent soft-glow painted around a focus shape. */
    public static Paint focusHalo(float cx, float cy, float radius) {
        Color g0 = new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(),
            Theme.ACCENT.getBlue(), 90);
        Color g1 = new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(),
            Theme.ACCENT.getBlue(), 0);
        return new RadialGradientPaint(
            new Point2D.Float(cx, cy),
            Math.max(1f, radius),
            new float[] { 0f, 1f },
            new Color[] { g0, g1 });
    }
}
