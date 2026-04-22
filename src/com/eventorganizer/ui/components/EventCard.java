package com.eventorganizer.ui.components;

import com.eventorganizer.models.Event;
import com.eventorganizer.models.enums.EventStatus;
import com.eventorganizer.models.enums.EventType;
import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.SoftBorder;
import com.eventorganizer.ui.theme.Spacing;
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
import java.awt.Insets;

public class EventCard extends JPanel {

    public EventCard(Event event, Runnable onOpen) {
        setLayout(new BorderLayout(Spacing.L, 0));
        setOpaque(true);
        setBackground(Theme.BG_ELEVATED);
        setBorder(SoftBorder.of(Radius.LG, Theme.BORDER, 1,
            new Insets(Spacing.L, Spacing.L, Spacing.L, Spacing.L)));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.S, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel(event.getName());
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        titleRow.add(title);
        titleRow.add(new Badge(event.getType().toString(),
            event.getType() == EventType.PUBLIC ? Badge.Kind.ACCENT : Badge.Kind.INFO));
        titleRow.add(new Badge(cardStatusLabel(event), cardStatusKind(event)));

        JLabel when = new JLabel(DateUtil.format(event.getDateTime())
            + "  •  " + event.getLocation());
        when.setFont(Theme.FONT_SMALL);
        when.setForeground(Theme.TEXT_MUTED);
        when.setAlignmentX(Component.LEFT_ALIGNMENT);
        when.setBorder(BorderFactory.createEmptyBorder(Spacing.S, 0, 0, 0));

        left.add(titleRow);
        left.add(when);

        JButton open = new JButton("Open");
        open.setMnemonic('O');
        open.addActionListener(e -> onOpen.run());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, 0));
        right.setOpaque(false);
        right.add(open);

        add(left, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
        add(Box.createVerticalStrut(0), BorderLayout.SOUTH);
    }

    private static String cardStatusLabel(Event e) {
        if (e.getStatus() == EventStatus.CANCELLED) return "CANCELLED";
        return e.isPast() ? "PAST" : "UPCOMING";
    }

    private static Badge.Kind cardStatusKind(Event e) {
        if (e.getStatus() == EventStatus.CANCELLED) return Badge.Kind.DANGER;
        return e.isPast() ? Badge.Kind.DEFAULT : Badge.Kind.SUCCESS;
    }
}
