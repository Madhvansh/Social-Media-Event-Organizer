package com.eventorganizer.ui.components;

import com.eventorganizer.ui.theme.Theme;

import javax.swing.JComponent;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

/**
 * Minimal line sparkline with a gradient fill under the curve. Hand-painted
 * {@link Path2D}; no charting library. Tolerates empty/single-point data by
 * drawing a flat baseline.
 */
public final class Sparkline extends JComponent {

    private final float[] data;
    private final Color lineColor;
    private final Color fillColor;

    public Sparkline(float[] data) {
        this(data, Theme.ACCENT);
    }

    public Sparkline(float[] data, Color lineColor) {
        this.data = data == null ? new float[0] : data.clone();
        this.lineColor = lineColor;
        this.fillColor = lineColor;
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredSize() { return new Dimension(160, 36); }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth(), h = getHeight();
        if (w <= 2 || h <= 2) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (data.length < 2) {
                g2.setColor(new Color(lineColor.getRed(), lineColor.getGreen(),
                    lineColor.getBlue(), 120));
                g2.setStroke(new BasicStroke(1.5f));
                int y = h - 4;
                g2.drawLine(2, y, w - 2, y);
                return;
            }

            float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY;
            for (float v : data) {
                if (v < min) min = v;
                if (v > max) max = v;
            }
            if (max - min < 0.001f) { min -= 0.5f; max += 0.5f; }

            float padY = 4f;
            float usableH = h - padY * 2;
            float stepX = (float) (w - 4) / (data.length - 1);

            Path2D.Float line = new Path2D.Float();
            Path2D.Float fill = new Path2D.Float();
            float baseline = h - padY;
            fill.moveTo(2, baseline);

            for (int i = 0; i < data.length; i++) {
                float x = 2 + i * stepX;
                float t = (data[i] - min) / (max - min);
                float y = padY + (1 - t) * usableH;
                if (i == 0) line.moveTo(x, y); else line.lineTo(x, y);
                fill.lineTo(x, y);
            }
            fill.lineTo(2 + (data.length - 1) * stepX, baseline);
            fill.closePath();

            GradientPaint grad = new GradientPaint(0, padY,
                new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), 96),
                0, baseline,
                new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), 0));
            g2.setPaint(grad);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
            g2.fill(fill);

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g2.setColor(lineColor);
            g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(line);
        } finally {
            g2.dispose();
        }
    }
}
