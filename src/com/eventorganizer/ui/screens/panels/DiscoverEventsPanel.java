package com.eventorganizer.ui.screens.panels;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.Event;
import com.eventorganizer.ui.components.EventCard;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.dialogs.EventDetailsDialog;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

public class DiscoverEventsPanel extends JPanel {

    private final UIController controller;
    private final JPanel listPanel = new JPanel();
    private final Runnable onChange;

    public DiscoverEventsPanel(UIController controller, Runnable onChange) {
        this.controller = controller;
        this.onChange = onChange;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PRIMARY);

        JLabel title = new JLabel("Discover");
        title.setFont(Theme.FONT_DISPLAY);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Theme.BG_PRIMARY);
        listPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));

        add(title, BorderLayout.NORTH);
        add(new JScrollPane(listPanel), BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        try {
            listPanel.removeAll();
            List<Event> events = controller.eventsInvitedTo();
            if (events.isEmpty()) {
                JLabel empty = new JLabel("No pending invitations right now.", SwingConstants.CENTER);
                empty.setForeground(Theme.TEXT_MUTED);
                empty.setFont(Theme.FONT_BODY);
                empty.setAlignmentX(Component.LEFT_ALIGNMENT);
                empty.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
                listPanel.add(empty);
            } else {
                for (Event e : events) {
                    EventCard card = new EventCard(e, () ->
                        EventDetailsDialog.show(this, controller, e.getEventId(),
                            () -> { refresh(); onChange.run(); }));
                    card.setAlignmentX(Component.LEFT_ALIGNMENT);
                    listPanel.add(card);
                    listPanel.add(Box.createVerticalStrut(8));
                }
            }
            listPanel.add(Box.createVerticalGlue());
            listPanel.revalidate();
            listPanel.repaint();
        } catch (AppException ex) {
            Toast.error(this, ex.getMessage());
        }
    }
}
