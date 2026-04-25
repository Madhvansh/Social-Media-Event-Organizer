package com.eventorganizer.ui.screens.panels;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.Event;
import com.eventorganizer.ui.components.CardGrid;
import com.eventorganizer.ui.components.EmptyState;
import com.eventorganizer.ui.components.EventCard;
import com.eventorganizer.ui.components.SegmentedControl;
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
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Discover panel — shows two feeds via a {@link SegmentedControl}:
 * <ul>
 *   <li><b>Public</b> — every active, upcoming public event the user didn't
 *       create (per Q7 spec: public events are visible to everyone).</li>
 *   <li><b>Invited</b> — events the user has a pending invitation to.</li>
 * </ul>
 * Cards in both feeds use {@link EventCard.Variant#INCOMING} (amethyst accent)
 * to visually differentiate them from the user's own events.
 */
public class DiscoverEventsPanel extends JPanel {

    private final UIController controller;
    private final Runnable onChange;

    private final SegmentedControl segs = new SegmentedControl();
    private final CardLayout views = new CardLayout();
    private final JPanel viewPanel = new JPanel(views);
    private final CardGrid publicGrid = new CardGrid();
    private final CardGrid invitedGrid = new CardGrid();

    public DiscoverEventsPanel(UIController controller, Runnable onChange) {
        this.controller = controller;
        this.onChange = onChange;
        setOpaque(false);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Discover");
        title.setFont(Typography.DISPLAY);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Browse public events and check incoming invitations.");
        sub.setFont(Typography.BODY);
        sub.setForeground(Theme.TEXT_SECONDARY);
        sub.setBorder(BorderFactory.createEmptyBorder(Spacing.XS, 0, Spacing.M, 0));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        title.setAlignmentX(LEFT_ALIGNMENT);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        header.add(title);
        header.add(sub);
        header.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, 0, Spacing.XL));

        segs.addSegment("Public", 0);
        segs.addSegment("Invited", 0);
        segs.onChange(idx -> views.show(viewPanel, idx == 0 ? "public" : "invited"));

        JPanel segsWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        segsWrap.setOpaque(false);
        segsWrap.setBorder(BorderFactory.createEmptyBorder(Spacing.S, Spacing.XL, Spacing.L, Spacing.XL));
        segsWrap.add(segs);

        publicGrid.setOpaque(false);
        invitedGrid.setOpaque(false);

        viewPanel.setOpaque(false);
        viewPanel.add(scrollOf(publicGrid),  "public");
        viewPanel.add(scrollOf(invitedGrid), "invited");

        JPanel topStack = new JPanel(new BorderLayout());
        topStack.setOpaque(false);
        topStack.add(header,   BorderLayout.NORTH);
        topStack.add(segsWrap, BorderLayout.CENTER);

        add(topStack, BorderLayout.NORTH);
        add(viewPanel, BorderLayout.CENTER);

        refresh();
    }

    private JScrollPane scrollOf(JPanel inner) {
        JScrollPane sp = new JScrollPane(inner);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.XL, Spacing.XL));
        sp.getVerticalScrollBar().setUnitIncrement(24);
        return sp;
    }

    public void refresh() {
        try {
            List<Event> publics = controller.discoverPublicEvents();
            List<Event> invited = controller.eventsInvitedTo();

            // Dedup: an event the user is invited to AND is public should only
            // appear once in the Public tab to avoid double-listing.
            Set<String> inInvited = new HashSet<>();
            for (Event e : invited) inInvited.add(e.getEventId());
            // Order public-but-not-invited first; keep invited list as-is.
            Map<String, Event> publicMap = new LinkedHashMap<>();
            for (Event e : publics) publicMap.put(e.getEventId(), e);

            renderGrid(publicGrid, publics,
                new EmptyState("compass", "No public events yet",
                    "When other users create public events, they'll appear here."));
            renderGrid(invitedGrid, invited,
                new EmptyState("envelope", "Nothing pending",
                    "Invitations to private events will show up here."));

            segs.setCount(0, publics.size());
            segs.setCount(1, invited.size());
        } catch (AppException ex) {
            Toast.error(this, ex.getMessage());
        }
    }

    private void renderGrid(CardGrid grid, List<Event> events, EmptyState empty) {
        grid.removeAll();
        if (events.isEmpty()) {
            grid.add(empty);
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
    }
}
