package com.eventorganizer.ui.components;

import com.eventorganizer.ui.theme.Typography;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Label with explicit letter-spacing (tracking). Swing's standard JLabel
 * ignores {@code TextAttribute.TRACKING} on plain strings; this component
 * renders through {@link Typography#drawTracked}.
 *
 * <p>Typical use: tracked uppercase section labels ({@code MY EVENTS},
 * {@code UPCOMING}) above hero numerals.
 */
public final class TrackedLabel extends JComponent {

    private String text;
    private Font font;
    private Color color;
    private float tracking;

    public TrackedLabel(String text, Font font, Color color, float tracking) {
        this.text = text == null ? "" : text;
        this.font = font;
        this.color = color;
        this.tracking = tracking;
        setOpaque(false);
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
        revalidate();
        repaint();
    }

    public String getText() { return text; }

    public void setColor(Color c) { this.color = c; repaint(); }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(font);
        int w = (int) Math.ceil(text.length() * (fm.charWidth('M') * (1f + tracking))) + 8;
        int h = fm.getHeight() + 4;
        return new Dimension(w, h);
    }

    @Override
    public Dimension getMinimumSize() { return getPreferredSize(); }

    @Override
    protected void paintComponent(Graphics g) {
        if (text.isEmpty()) return;
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2.setColor(color);
            FontMetrics fm = g2.getFontMetrics(font);
            Typography.drawTracked(g2, text, font, tracking, 0, fm.getAscent());
        } finally {
            g2.dispose();
        }
    }
}
