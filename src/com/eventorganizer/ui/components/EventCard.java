package com.eventorganizer.ui.components;

import com.eventorganizer.models.Event;
import com.eventorganizer.models.enums.EventStatus;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.utils.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class EventCard extends JPanel {

    public EventCard(Event event, Runnable onOpen) {
        setLayout(new BorderLayout(12, 0));
        setBackground(Theme.BG_ELEVATED);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(event.getName());
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel when = new JLabel(DateUtil.format(event.getDateTime()) + "  -  " + event.getLocation());
        when.setFont(Theme.FONT_SMALL);
        when.setForeground(Theme.TEXT_MUTED);
        when.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        badges.setOpaque(false);
        badges.add(new Badge(event.getType().toString(), Theme.ACCENT));
        badges.add(new Badge(event.getStatus().toString(),
            event.getStatus() == EventStatus.CANCELLED ? Theme.DANGER : Theme.SUCCESS));
        badges.setAlignmentX(Component.LEFT_ALIGNMENT);

        left.add(title);
        left.add(Box.createVerticalStrut(4));
        left.add(when);
        left.add(Box.createVerticalStrut(2));
        left.add(badges);

        JButton open = new JButton("Open");
        open.setMnemonic('O');
        open.addActionListener(e -> onOpen.run());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(open);

        add(left, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
    }
}
