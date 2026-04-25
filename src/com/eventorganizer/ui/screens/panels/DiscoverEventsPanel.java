package com.eventorganizer.ui.screens.panels;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.Event;
import com.eventorganizer.ui.components.CardGrid;
import com.eventorganizer.ui.components.EmptyState;
import com.eventorganizer.ui.components.EventCard;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.dialogs.EventDetailsDialog;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.util.List;

/**
 * Discover panel — public events you've been invited to. Same responsive card
 * grid as My Events but cards render with the {@link EventCard.Variant#INCOMING}
 * amethyst accent so the two feeds are visually distinct.
 */
public class DiscoverEventsPanel extends JPanel {

    private final UIController controller;
    private final CardGrid grid = new CardGrid();
    private final Runnable onChange;

    public DiscoverEventsPanel(UIController controller, Runnable onChange) {
        this.controller = controller;
        this.onChange = onChange;
        setOpaque(false);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Discover");
        title.setFont(Typography.DISPLAY);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.M, Spacing.XL));

        JLabel sub = new JLabel("Public events you've been invited to.");
        sub.setFont(Typography.BODY);
        sub.setForeground(Theme.TEXT_SECONDARY);
        sub.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.L, Spacing.XL));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.CENTER);

        grid.setOpaque(false);
        JScrollPane sp = new JScrollPane(grid);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.XL, Spacing.XL));
        sp.getVerticalScrollBar().setUnitIncrement(24);

        add(header, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        try {
            grid.removeAll();
            List<Event> events = controller.eventsInvitedTo();
            if (events.isEmpty()) {
                grid.add(new EmptyState("envelope", "Nothing to discover yet",
                    "Public events you've been invited to will show up here."));
            } else {
                for (Event e : events) {
                    grid.addCard(new EventCard(e, () ->
                        EventDetailsDialog.show(this, controller, e.getEventId(),
                            () -> { refresh(); if (onChange != null) onChange.run(); }),
                        EventCard.Variant.INCOMING));
                }
            }
            grid.revalidate();
            grid.repaint();
        } catch (AppException ex) {
            Toast.error(this, ex.getMessage());
        }
    }
}
