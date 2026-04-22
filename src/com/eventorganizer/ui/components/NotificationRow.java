package com.eventorganizer.ui.components;

import com.eventorganizer.models.Notification;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.utils.TimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

public class NotificationRow extends JPanel implements ListCellRenderer<Notification> {

    private final JLabel unreadDot = new JLabel("*");
    private final JLabel message   = new JLabel();
    private final JLabel category  = new JLabel();
    private final JLabel time      = new JLabel();

    public NotificationRow() {
        setLayout(new BorderLayout(8, 0));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        unreadDot.setForeground(Theme.ACCENT);
        unreadDot.setFont(Theme.FONT_TITLE);
        unreadDot.setPreferredSize(new Dimension(14, 14));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        category.setFont(Theme.FONT_SMALL);
        message.setFont(Theme.FONT_BODY);
        center.add(category);
        center.add(message);

        time.setFont(Theme.FONT_SMALL);
        time.setForeground(Theme.TEXT_MUTED);

        add(unreadDot, BorderLayout.WEST);
        add(center,    BorderLayout.CENTER);
        add(time,      BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Notification> list,
                                                  Notification n, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        if (n == null) return this;
        boolean unread = !n.isRead();
        unreadDot.setText(unread ? "*" : " ");
        category.setText(n.getCategory());
        category.setForeground(colorForCategory(n.getCategory()));
        message.setText(n.getMessage());
        message.setForeground(Theme.TEXT_PRIMARY);
        message.setFont(unread ? Theme.FONT_BODY.deriveFont(Font.BOLD) : Theme.FONT_BODY);
        time.setText(TimeFormatter.relative(n.getTimestamp()));
        setBackground(isSelected ? Theme.BG_HOVER : Theme.BG_ELEVATED);
        setOpaque(true);
        return this;
    }

    private Color colorForCategory(String cat) {
        if (cat == null) return Theme.TEXT_MUTED;
        switch (cat) {
            case "INVITATION":     return Theme.ACCENT;
            case "RSVP":           return Theme.SUCCESS;
            case "EVENT UPDATE":   return Theme.WARNING;
            case "FRIEND REQUEST": return Theme.ACCENT_HOVER;
            default:               return Theme.TEXT_MUTED;
        }
    }
}
