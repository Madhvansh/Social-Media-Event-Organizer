package com.eventorganizer.ui.components;

import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.SoftBorder;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

/**
 * A single friend-list row: 32x32 rounded initials avatar on BG_HOVER,
 * username + optional subtitle, and a right-aligned action slot the caller
 * fills with buttons.
 */
public class FriendRow extends JPanel {

    private final JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, 0));

    public FriendRow(String username, String subtitle) {
        setLayout(new BorderLayout(Spacing.M, 0));
        setOpaque(true);
        setBackground(Theme.BG_ELEVATED);
        setBorder(SoftBorder.of(Radius.MD, Theme.BORDER_SUBTLE, 1,
            new Insets(Spacing.M, Spacing.M, Spacing.M, Spacing.M)));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        Avatar avatar = new Avatar(initials(username));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new javax.swing.BoxLayout(text, javax.swing.BoxLayout.Y_AXIS));
        JLabel name = new JLabel(username);
        name.setFont(Theme.FONT_BODY_BOLD);
        name.setForeground(Theme.TEXT_PRIMARY);
        text.add(name);
        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel sub = new JLabel(subtitle);
            sub.setFont(Theme.FONT_SMALL);
            sub.setForeground(Theme.TEXT_MUTED);
            text.add(sub);
        }

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.M, 0));
        left.setOpaque(false);
        left.add(avatar);
        left.add(text);

        actions.setOpaque(false);

        add(left, BorderLayout.WEST);
        add(actions, BorderLayout.EAST);
    }

    public FriendRow addAction(java.awt.Component c) {
        actions.add(c);
        return this;
    }

    private static String initials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String n = name.trim();
        char c = Character.toUpperCase(n.charAt(0));
        return String.valueOf(c);
    }

    private static final class Avatar extends JPanel {
        private final String letter;
        Avatar(String letter) {
            this.letter = letter;
            setOpaque(false);
            setPreferredSize(new Dimension(32, 32));
        }
        @Override
        public Color getBackground() { return Theme.BG_HOVER; }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Theme.ACCENT);
                g2.setFont(Theme.FONT_BODY_BOLD);
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(letter)) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(letter, tx, ty);
            } finally {
                g2.dispose();
            }
        }
    }
}
