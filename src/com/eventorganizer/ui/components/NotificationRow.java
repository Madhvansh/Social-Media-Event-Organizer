package com.eventorganizer.ui.components;

import com.eventorganizer.models.Notification;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.utils.TimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class NotificationRow extends JPanel implements ListCellRenderer<Notification> {

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
            BorderFactory.createEmptyBorder(Spacing.M, Spacing.L + 3, Spacing.M, Spacing.L)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.S, 0));
        left.setOpaque(false);
        left.add(categoryBadge);
        left.add(message);

        message.setForeground(Theme.TEXT_PRIMARY);
        message.setFont(Theme.FONT_BODY);

        time.setFont(Theme.FONT_SMALL);
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

        message.setText(n.getMessage());
        message.setFont(unread ? Theme.FONT_BODY_BOLD : Theme.FONT_BODY);
        message.setForeground(unread ? Theme.TEXT_PRIMARY : Theme.TEXT_MUTED);
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
        if (unread) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.ACCENT);
                g2.fillRect(0, 0, 3, getHeight());
            } finally {
                g2.dispose();
            }
        }
    }

    private Badge.Kind kindForCategory(String cat) {
        if (cat == null) return Badge.Kind.DEFAULT;
        switch (cat) {
            case "INVITATION":     return Badge.Kind.ACCENT;
            case "RSVP":           return Badge.Kind.SUCCESS;
            case "EVENT UPDATE":   return Badge.Kind.WARNING;
            case "FRIEND REQUEST": return Badge.Kind.INFO;
            default:               return Badge.Kind.DEFAULT;
        }
    }
}
