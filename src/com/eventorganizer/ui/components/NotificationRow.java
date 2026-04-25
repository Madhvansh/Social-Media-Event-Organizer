package com.eventorganizer.ui.components;

import com.eventorganizer.models.Notification;
import com.eventorganizer.ui.theme.Gradient;
import com.eventorganizer.ui.theme.Iconography;
import com.eventorganizer.ui.theme.Motion;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;
import com.eventorganizer.utils.TimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Editorial notification row. Left: category glyph (from {@link Iconography}),
 * then category pill, then the message body. Right: relative timestamp in mono.
 *
 * <p>Unread rows get bold body + a pulsing bronze dot. Selected rows gain an
 * {@code ACCENT_SOFT} wash plus a left bronze-sweep rail.
 */
public class NotificationRow extends JPanel implements ListCellRenderer<Notification> {

    private final IconCell icon = new IconCell();
    private final Badge categoryBadge = new Badge("", Badge.Kind.DEFAULT);
    private final JLabel message = new JLabel();
    private final JLabel time = new JLabel();
    private boolean unread;
    private boolean selected;

    public NotificationRow() {
        setLayout(new BorderLayout(Spacing.M, 0));
        setOpaque(true);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(Spacing.M, Spacing.L + 6, Spacing.M, Spacing.L)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.S, 0));
        left.setOpaque(false);
        left.add(icon);
        left.add(categoryBadge);
        left.add(message);

        message.setForeground(Theme.TEXT_PRIMARY);
        message.setFont(Typography.BODY);

        time.setFont(Typography.MONO);
        time.setForeground(Theme.TEXT_TERTIARY);

        add(left, BorderLayout.CENTER);
        add(time, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Notification> list,
                                                  Notification n, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        if (n == null) return this;
        unread = !n.isRead();
        selected = isSelected;

        String cat = n.getCategory();
        categoryBadge.setText(cat);
        categoryBadge.setKind(kindForCategory(cat));
        icon.setGlyph(iconForCategory(cat));
        icon.setColor(colorForCategory(cat));
        icon.setUnread(unread);

        message.setText(SwingText.plain(n.getMessage()));
        message.setFont(unread ? Typography.BODY_BOLD : Typography.BODY);
        message.setForeground(unread ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
        time.setText(TimeFormatter.relative(n.getTimestamp()));
        return this;
    }

    @Override
    public java.awt.Color getBackground() {
        if (selected) return Theme.ACCENT_SOFT;
        return Theme.BG_PRIMARY;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (selected) {
                g2.setPaint(Gradient.bronzeSweep(4, getHeight()));
                g2.fillRect(0, 0, 4, getHeight());
            } else if (unread) {
                g2.setColor(Theme.ACCENT);
                g2.fillRect(0, 0, 3, getHeight());
            }
        } finally {
            g2.dispose();
        }
    }

    private Badge.Kind kindForCategory(String cat) {
        if (cat == null) return Badge.Kind.DEFAULT;
        switch (cat) {
            case "INVITATION":     return Badge.Kind.ACCENT;
            case "RSVP":           return Badge.Kind.SUCCESS;
            case "EVENT UPDATE":   return Badge.Kind.WARNING;
            case "FRIEND REQUEST": return Badge.Kind.ACCENT2;
            default:               return Badge.Kind.DEFAULT;
        }
    }

    private String iconForCategory(String cat) {
        if (cat == null) return "bell";
        switch (cat) {
            case "INVITATION":     return "envelope";
            case "RSVP":           return "check";
            case "EVENT UPDATE":   return "calendar";
            case "FRIEND REQUEST": return "users";
            default:               return "bell";
        }
    }

    private Color colorForCategory(String cat) {
        if (cat == null) return Theme.TEXT_SECONDARY;
        switch (cat) {
            case "INVITATION":     return Theme.ACCENT;
            case "RSVP":           return Theme.SUCCESS;
            case "EVENT UPDATE":   return Theme.WARNING;
            case "FRIEND REQUEST": return Theme.ACCENT2;
            default:               return Theme.TEXT_SECONDARY;
        }
    }

    /** Tiny icon tile with optional pulse-dot overlay for unread. */
    private static final class IconCell extends javax.swing.JComponent {
        private String glyph = "bell";
        private Color color = Theme.TEXT_SECONDARY;
        private boolean unread;
        IconCell() {
            java.awt.Dimension d = new java.awt.Dimension(22, 22);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }
        void setGlyph(String g) { this.glyph = g; repaint(); }
        void setColor(Color c) { this.color = c; repaint(); }
        void setUnread(boolean u) { this.unread = u; repaint(); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                Iconography.paint(g2, glyph, 2, 2, 18f, color);
                if (unread && !Motion.REDUCED) {
                    g2.setColor(new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(),
                        Theme.ACCENT.getBlue(), 120));
                    g2.fillOval(15, 0, 7, 7);
                    g2.setColor(Theme.ACCENT);
                    g2.fillOval(16, 1, 5, 5);
                }
            } finally {
                g2.dispose();
            }
        }
    }
}
