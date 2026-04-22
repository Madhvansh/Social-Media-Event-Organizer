package com.eventorganizer.ui.screens.panels;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.Event;
import com.eventorganizer.models.enums.EventStatus;
import com.eventorganizer.ui.components.EmptyState;
import com.eventorganizer.ui.components.EventCard;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.theme.Spacing;
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
    private final JTabbedPane tabs = new JTabbedPane();
    private final Runnable onChange;

    public MyEventsPanel(UIController controller, Runnable onChange) {
        this.controller = controller;
        this.onChange = onChange;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PRIMARY);

        JLabel title = new JLabel("My Events");
        title.setFont(Theme.FONT_DISPLAY);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(Spacing.L, Spacing.XL, Spacing.XS, Spacing.XL));

        JButton create = new JButton("Create Event");
        create.setMnemonic('N');
        create.putClientProperty("JButton.buttonType", "default");
        create.addActionListener(e -> CreateEventDialog.show(this, controller, this::refresh));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, 0));
        right.setOpaque(false);
        right.add(create);
        right.setBorder(BorderFactory.createEmptyBorder(Spacing.S, 0, 0, Spacing.L));
        top.add(right, BorderLayout.EAST);

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
            int upcomingN = fill(upcomingList, all, e -> e.isUpcoming() && e.getStatus() != EventStatus.CANCELLED,
                new EmptyState("+", "No upcoming events",
                    "Click 'Create Event' to plan something."));
            int pastN = fill(pastList, all, Event::isPast,
                new EmptyState("•", "No past events",
                    "Events you've hosted will appear here after they end."));
            int cancelledN = fill(cancelledList, all, e -> e.getStatus() == EventStatus.CANCELLED,
                new EmptyState("×", "No cancelled events",
                    "Cancelled events are archived here for reference."));
            tabs.setTitleAt(0, tabLabel("Upcoming",  upcomingN));
            tabs.setTitleAt(1, tabLabel("Past",      pastN));
            tabs.setTitleAt(2, tabLabel("Cancelled", cancelledN));
        } catch (AppException ex) {
            Toast.error(this, ex.getMessage());
        }
    }

    private static String tabLabel(String name, int count) {
        return count == 0 ? name : name + " (" + count + ")";
    }

    private int fill(JPanel container, List<Event> all, Predicate<Event> filter, EmptyState emptyPlaceholder) {
        container.removeAll();
        int matched = 0;
        for (Event e : all) {
            if (!filter.test(e)) continue;
            matched++;
            EventCard card = new EventCard(e, () ->
                EventDetailsDialog.show(this, controller, e.getEventId(), this::refreshAll));
            card.setAlignmentX(Component.LEFT_ALIGNMENT);
            container.add(card);
            container.add(Box.createVerticalStrut(Spacing.M));
        }
        if (matched == 0) {
            emptyPlaceholder.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.add(emptyPlaceholder);
        }
        container.add(Box.createVerticalGlue());
        container.revalidate();
        container.repaint();
        return matched;
    }

    private void refreshAll() { refresh(); onChange.run(); }

    private JPanel listPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.BG_PRIMARY);
        p.setBorder(BorderFactory.createEmptyBorder(Spacing.L, Spacing.L, Spacing.L, Spacing.L));
        return p;
    }
}
