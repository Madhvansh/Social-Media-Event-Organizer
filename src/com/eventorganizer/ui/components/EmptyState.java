package com.eventorganizer.ui.components;

import com.eventorganizer.ui.laf.AuroraButton;
import com.eventorganizer.ui.theme.Iconography;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Centered "nothing here yet" placeholder. Unified look across every panel:
 * glyph disc on BG_OVERLAY, title, muted body copy, optional primary action.
 *
 * <p>Accepts either a single-character string or an Iconography path name as
 * {@code glyph}. If the glyph matches a name in {@link Iconography}, it renders
 * as a stroked path; otherwise it's rendered as text (legacy behaviour).
 */
public class EmptyState extends JPanel {

    public EmptyState(String glyph, String title, String body) {
        this(glyph, title, body, null, null);
    }

    public EmptyState(String glyph, String title, String body, String actionLabel, Runnable onAction) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(Spacing.XXL, Spacing.XL, Spacing.XXL, Spacing.XL));
        setAlignmentX(Component.CENTER_ALIGNMENT);

        GlyphDisc disc = new GlyphDisc(glyph);
        disc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Typography.H2);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(disc);
        add(Box.createVerticalStrut(Spacing.M));
        add(titleLabel);

        if (body != null && !body.isEmpty()) {
            JLabel bodyLabel = new JLabel(body);
            bodyLabel.setFont(Typography.BODY);
            bodyLabel.setForeground(Theme.TEXT_SECONDARY);
            bodyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(Box.createVerticalStrut(Spacing.XS));
            add(bodyLabel);
        }

        if (actionLabel != null && onAction != null) {
            AuroraButton btn = new AuroraButton(actionLabel, AuroraButton.Variant.DEFAULT);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.addActionListener(e -> onAction.run());
            add(Box.createVerticalStrut(Spacing.L));
            add(btn);
        }
    }

    private static final class GlyphDisc extends JPanel {
        private final String glyph;
        GlyphDisc(String glyph) {
            this.glyph = glyph == null ? "" : glyph;
            setOpaque(false);
            Dimension d = new Dimension(72, 72);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

                // Outer soft halo
                g2.setColor(Theme.ACCENT_SOFT);
                g2.fillOval(0, 0, getWidth(), getHeight());
                // Inner disc
                g2.setColor(Theme.BG_OVERLAY);
                int inset = 8;
                g2.fillOval(inset, inset, getWidth() - inset * 2, getHeight() - inset * 2);
                // Ring
                g2.setColor(Theme.BORDER);
                g2.drawOval(inset, inset, getWidth() - inset * 2 - 1, getHeight() - inset * 2 - 1);

                if (Iconography.has(glyph)) {
                    float iconSize = 28f;
                    float x = (getWidth() - iconSize) / 2f;
                    float y = (getHeight() - iconSize) / 2f;
                    Iconography.paint(g2, glyph, x, y, iconSize, Theme.ACCENT);
                } else {
                    g2.setColor(new Color(Theme.TEXT_SECONDARY.getRed(),
                        Theme.TEXT_SECONDARY.getGreen(),
                        Theme.TEXT_SECONDARY.getBlue(), 200));
                    g2.setFont(Typography.H1);
                    java.awt.FontMetrics fm = g2.getFontMetrics();
                    int tx = (getWidth() - fm.stringWidth(glyph)) / 2;
                    int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(glyph, tx, ty);
                }
            } finally {
                g2.dispose();
            }
        }
    }
}
