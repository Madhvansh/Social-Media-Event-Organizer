package com.eventorganizer.ui.screens.panels;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.Event;
import com.eventorganizer.models.enums.EventStatus;
import com.eventorganizer.ui.components.EventCard;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.dialogs.CreateEventDialog;
import com.eventorganizer.ui.dialogs.EventDetailsDialog;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.List;
import java.util.function.Predicate;

public class MyEventsPanel extends JPanel {

    private final UIController controller;
    private final JPanel upcomingList = listPanel();
    private final JPanel pastList     = listPanel();
    private final JPanel cancelledList= listPanel();
    private final Runnable onChange;

    public MyEventsPanel(UIController controller, Runnable onChange) {
        this.controller = controller;
        this.onChange = onChange;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PRIMARY);

        JLabel title = new JLabel("My Events");
        title.setFont(Theme.FONT_DISPLAY);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(16, 20, 4, 20));

        JButton create = new JButton("Create Event");
        create.setMnemonic('N');
        create.addActionListener(e -> CreateEventDialog.show(this, controller, this::refresh));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(create);
        right.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 16));
        top.add(right, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Upcoming",  new JScrollPane(upcomingList));
        tabs.addTab("Past",      new JScrollPane(pastList));
        tabs.addTab("Cancelled", new JScrollPane(cancelledList));

        add(top, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        try {
            List<Event> all = controller.myEvents();
            fill(upcomingList,  all, e -> e.isUpcoming() && e.getStatus() != EventStatus.CANCELLED,
                "No upcoming events yet. Click 'Create Event' to start.");
            fill(pastList,      all, Event::isPast,
                "No past events yet.");
            fill(cancelledList, all, e -> e.getStatus() == EventStatus.CANCELLED,
                "No cancelled events.");
        } catch (AppException ex) {
            Toast.error(this, ex.getMessage());
        }
    }

    private void fill(JPanel container, List<Event> all, Predicate<Event> filter, String emptyMessage) {
        container.removeAll();
        boolean any = false;
        for (Event e : all) {
            if (!filter.test(e)) continue;
            any = true;
            EventCard card = new EventCard(e, () ->
                EventDetailsDialog.show(this, controller, e.getEventId(), this::refreshAll));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            container.add(card);
            container.add(Box.createVerticalStrut(8));
        }
        if (!any) container.add(emptyState(emptyMessage));
        container.add(Box.createVerticalGlue());
        container.revalidate();
        container.repaint();
    }

    private void refreshAll() { refresh(); onChange.run(); }

    private JPanel listPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.BG_PRIMARY);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        return p;
    }

    private JLabel emptyState(String msg) {
        JLabel l = new JLabel(msg, SwingConstants.CENTER);
        l.setForeground(Theme.TEXT_MUTED);
        l.setFont(Theme.FONT_BODY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        return l;
    }
}
