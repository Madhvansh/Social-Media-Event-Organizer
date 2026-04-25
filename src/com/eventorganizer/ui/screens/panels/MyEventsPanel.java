package com.eventorganizer.ui.screens.panels;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.Event;
import com.eventorganizer.models.enums.EventStatus;
import com.eventorganizer.ui.components.CardGrid;
import com.eventorganizer.ui.components.EmptyState;
import com.eventorganizer.ui.components.EventCard;
import com.eventorganizer.ui.components.SegmentedControl;
import com.eventorganizer.ui.components.TimelineSpine;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.dialogs.CreateEventDialog;
import com.eventorganizer.ui.dialogs.EventDetailsDialog;
import com.eventorganizer.ui.laf.AuroraButton;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Editorial My Events panel. Uses a {@link SegmentedControl} to flip between
 * three views. Upcoming renders through {@link TimelineSpine}; Past and
 * Cancelled use a two-column {@link CardGrid}.
 */
public class MyEventsPanel extends JPanel {

    private final UIController controller;
    private final Runnable onChange;

    private final SegmentedControl segs = new SegmentedControl();
    private final CardLayout views = new CardLayout();
    private final JPanel viewPanel = new JPanel(views);

    private final JPanel upcomingHolder = new JPanel(new BorderLayout());
    private final CardGrid pastGrid = new CardGrid();
    private final CardGrid cancelledGrid = new CardGrid();

    public MyEventsPanel(UIController controller, Runnable onChange) {
        this.controller = controller;
        this.onChange = onChange;
        setOpaque(false);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("My Events");
        title.setFont(Typography.DISPLAY);
        title.setForeground(Theme.TEXT_PRIMARY);

        AuroraButton create = new AuroraButton("Create Event", AuroraButton.Variant.DEFAULT);
        create.setMnemonic('N');
        create.addActionListener(e -> CreateEventDialog.show(this, controller, this::refreshAll));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.M, Spacing.XL));
        top.add(title, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, 0));
        right.setOpaque(false);
        right.add(create);
        top.add(right, BorderLayout.EAST);

        segs.addSegment("Upcoming", 0);
        segs.addSegment("Past", 0);
        segs.addSegment("Cancelled", 0);
        segs.onChange(idx -> views.show(viewPanel,
            idx == 0 ? "upcoming" : idx == 1 ? "past" : "cancelled"));

        JPanel segsWrap = new JPanel(new BorderLayout());
        segsWrap.setOpaque(false);
        segsWrap.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.L, Spacing.XL));
        segsWrap.add(segs, BorderLayout.WEST);

        upcomingHolder.setOpaque(false);
        pastGrid.setOpaque(false);
        cancelledGrid.setOpaque(false);

        viewPanel.setOpaque(false);
        viewPanel.add(scrollOf(upcomingHolder), "upcoming");
        viewPanel.add(scrollOf(pastGrid),       "past");
        viewPanel.add(scrollOf(cancelledGrid),  "cancelled");

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(top,      BorderLayout.NORTH);
        header.add(segsWrap, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(viewPanel, BorderLayout.CENTER);

        refresh();
    }

    private JScrollPane scrollOf(JPanel p) {
        JScrollPane sp = new JScrollPane(p);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.XL, Spacing.XL));
        sp.getVerticalScrollBar().setUnitIncrement(24);
        return sp;
    }

    public void refresh() {
        try {
            List<Event> all = controller.myEvents();

            List<Event> upcoming = filterAndSort(all,
                e -> e.isUpcoming() && e.getStatus() != EventStatus.CANCELLED,
                Comparator.comparing(Event::getDateTime));
            List<Event> past = filterAndSort(all, Event::isPast,
                Comparator.comparing(Event::getDateTime, Comparator.reverseOrder()));
            List<Event> cancelled = filterAndSort(all,
                e -> e.getStatus() == EventStatus.CANCELLED,
                Comparator.comparing(Event::getDateTime, Comparator.reverseOrder()));

            fillUpcoming(upcoming);
            fillGrid(pastGrid, past, new EmptyState("calendar", "No past events",
                "Events you've hosted will appear here after they end."), EventCard.Variant.OWNED);
            fillGrid(cancelledGrid, cancelled, new EmptyState("x", "No cancelled events",
                "Cancelled events are archived here for reference."), EventCard.Variant.OWNED);

            segs.setCount(0, upcoming.size());
            segs.setCount(1, past.size());
            segs.setCount(2, cancelled.size());
        } catch (AppException ex) {
            Toast.error(this, ex.getMessage());
        }
    }

    private List<Event> filterAndSort(List<Event> in, Predicate<Event> filter, Comparator<Event> cmp) {
        List<Event> out = new ArrayList<>();
        for (Event e : in) if (filter.test(e)) out.add(e);
        out.sort(cmp);
        return out;
    }

    private void fillUpcoming(List<Event> upcoming) {
        upcomingHolder.removeAll();
        if (upcoming.isEmpty()) {
            upcomingHolder.add(new EmptyState("sparkle", "No upcoming events",
                "Click 'Create Event' to plan something."), BorderLayout.NORTH);
        } else {
            TimelineSpine spine = new TimelineSpine(upcoming, e ->
                EventDetailsDialog.show(this, controller, e.getEventId(), this::refreshAll));
            upcomingHolder.add(spine, BorderLayout.NORTH);
        }
        upcomingHolder.revalidate();
        upcomingHolder.repaint();
    }

    private void fillGrid(CardGrid grid, List<Event> events, EmptyState empty,
                          EventCard.Variant variant) {
        grid.removeAll();
        if (events.isEmpty()) {
            grid.add(empty);
        } else {
            for (Event e : events) {
                grid.addCard(new EventCard(e, () ->
                    EventDetailsDialog.show(this, controller, e.getEventId(), this::refreshAll),
                    variant));
            }
        }
        grid.revalidate();
        grid.repaint();
    }

    private void refreshAll() {
        refresh();
        if (onChange != null) onChange.run();
    }
}
