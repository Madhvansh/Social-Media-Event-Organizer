package com.eventorganizer.ui.components;

import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Centered "nothing here yet" placeholder. Unified look across every panel and
 * dialog that can run out of content: glyph disc on BG_HOVER, subtitle + muted
 * body copy, optional primary action.
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
        titleLabel.setFont(Theme.FONT_SUBTITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(disc);
        add(Box.createVerticalStrut(Spacing.M));
        add(titleLabel);

        if (body != null && !body.isEmpty()) {
            JLabel bodyLabel = new JLabel(body);
            bodyLabel.setFont(Theme.FONT_BODY);
            bodyLabel.setForeground(Theme.TEXT_MUTED);
            bodyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(Box.createVerticalStrut(Spacing.XS));
            add(bodyLabel);
        }

        if (actionLabel != null && onAction != null) {
            JButton btn = new JButton(actionLabel);
            btn.putClientProperty("JButton.buttonType", "default");
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
            Dimension d = new Dimension(56, 56);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setColor(Theme.BG_HOVER);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(Theme.TEXT_MUTED.getRed(), Theme.TEXT_MUTED.getGreen(),
                    Theme.TEXT_MUTED.getBlue(), 180));
                g2.setFont(Theme.FONT_DISPLAY);
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(glyph)) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(glyph, tx, ty);
            } finally {
                g2.dispose();
            }
        }
    }
}
