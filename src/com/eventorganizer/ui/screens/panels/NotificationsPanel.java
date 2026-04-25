package com.eventorganizer.ui.screens.panels;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.EventUpdateNotification;
import com.eventorganizer.models.FriendRequestNotification;
import com.eventorganizer.models.InvitationNotification;
import com.eventorganizer.models.Notification;
import com.eventorganizer.ui.components.AsyncUI;
import com.eventorganizer.ui.components.EmptyState;
import com.eventorganizer.ui.components.NotificationRow;
import com.eventorganizer.ui.components.SegmentedControl;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.dialogs.EventDetailsDialog;
import com.eventorganizer.ui.laf.AuroraButton;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Notifications feed. {@link SegmentedControl} filter at the top (All / Unread /
 * Invitations / RSVPs / Friends / Updates), then a list of {@link NotificationRow}
 * cells. Double-click deep-links to the underlying entity (event, friend tab).
 */
public class NotificationsPanel extends JPanel {

    private static final String CARD_LIST  = "list";
    private static final String CARD_EMPTY = "empty";

    private final UIController controller;
    private final DefaultListModel<Notification> model = new DefaultListModel<>();
    private final JList<Notification> list;
    private final Runnable onChange;
    private final CardLayout bodyLayout = new CardLayout();
    private final JPanel body = new JPanel(bodyLayout);

    private final SegmentedControl segs = new SegmentedControl();
    private Predicate<Notification> activeFilter = n -> true;

    public NotificationsPanel(UIController controller, Runnable onChange) {
        this.controller = controller;
        this.onChange = onChange;
        setLayout(new BorderLayout());
        setOpaque(false);

        JLabel title = new JLabel("Notifications");
        title.setFont(Typography.DISPLAY);
        title.setForeground(Theme.TEXT_PRIMARY);

        AuroraButton markAll = new AuroraButton("Mark all as read", AuroraButton.Variant.GHOST);
        markAll.setMnemonic('M');
        markAll.addActionListener(e -> AsyncUI.run(markAll,
            () -> controller.markAllRead(),
            () -> { refresh(); if (onChange != null) onChange.run(); },
            ex -> Toast.error(this, ex.getMessage())));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.M, Spacing.XL));
        top.add(title, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, 0));
        right.setOpaque(false);
        right.add(markAll);
        top.add(right, BorderLayout.EAST);

        segs.addSegment("All", 0);
        segs.addSegment("Unread", 0);
        segs.addSegment("Invitations", 0);
        segs.addSegment("RSVPs", 0);
        segs.addSegment("Friends", 0);
        segs.addSegment("Updates", 0);
        segs.onChange(idx -> {
            switch (idx) {
                case 1: activeFilter = n -> !n.isRead(); break;
                case 2: activeFilter = n -> n instanceof InvitationNotification; break;
                case 3: activeFilter = n -> n instanceof com.eventorganizer.models.RSVPNotification; break;
                case 4: activeFilter = n -> n instanceof FriendRequestNotification; break;
                case 5: activeFilter = n -> n instanceof EventUpdateNotification; break;
                case 0:
                default: activeFilter = n -> true;
            }
            refresh();
        });

        JPanel segsWrap = new JPanel(new BorderLayout());
        segsWrap.setOpaque(false);
        segsWrap.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.L, Spacing.XL));
        segsWrap.add(segs, BorderLayout.WEST);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(top, BorderLayout.NORTH);
        header.add(segsWrap, BorderLayout.SOUTH);

        list = new JList<>(model);
        list.setCellRenderer(new NotificationRow());
        list.setBackground(Theme.BG_PRIMARY);
        list.setFixedCellHeight(64);
        list.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2) return;
                Notification n = list.getSelectedValue();
                if (n == null) return;
                n.markAsRead();
                if (onChange != null) onChange.run();
                deepLink(n);
            }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.XL, Spacing.XL));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setPreferredSize(new Dimension(0, 0));
        scroll.getVerticalScrollBar().setUnitIncrement(24);

        JPanel emptyWrap = new JPanel(new java.awt.GridBagLayout());
        emptyWrap.setOpaque(false);
        emptyWrap.add(new EmptyState("check", "You're all caught up",
            "New invitations, RSVPs, and updates will appear here."));

        body.setOpaque(false);
        body.add(scroll, CARD_LIST);
        body.add(emptyWrap, CARD_EMPTY);

        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        try {
            List<Notification> all = controller.notifications();
            int allN = all.size();
            int unreadN = 0;
            int inviteN = 0, rsvpN = 0, friendN = 0, updateN = 0;
            for (Notification n : all) {
                if (!n.isRead()) unreadN++;
                if (n instanceof InvitationNotification) inviteN++;
                if (n instanceof com.eventorganizer.models.RSVPNotification) rsvpN++;
                if (n instanceof FriendRequestNotification) friendN++;
                if (n instanceof EventUpdateNotification) updateN++;
            }
            segs.setCount(0, allN);
            segs.setCount(1, unreadN);
            segs.setCount(2, inviteN);
            segs.setCount(3, rsvpN);
            segs.setCount(4, friendN);
            segs.setCount(5, updateN);

            List<Notification> filtered = new ArrayList<>();
            for (Notification n : all) if (activeFilter.test(n)) filtered.add(n);
            model.clear();
            for (Notification n : filtered) model.addElement(n);
            bodyLayout.show(body, filtered.isEmpty() ? CARD_EMPTY : CARD_LIST);
        } catch (AppException ex) {
            Toast.error(this, ex.getMessage());
        }
    }

    private void deepLink(Notification n) {
        String eventId = null;
        if (n instanceof InvitationNotification)   eventId = ((InvitationNotification) n).getEventId();
        else if (n instanceof EventUpdateNotification) eventId = ((EventUpdateNotification) n).getEventId();
        else if (n instanceof com.eventorganizer.models.RSVPNotification)
            eventId = ((com.eventorganizer.models.RSVPNotification) n).getEventId();
        if (eventId != null) {
            EventDetailsDialog.show(this, controller, eventId,
                () -> { refresh(); if (onChange != null) onChange.run(); });
        } else if (n instanceof FriendRequestNotification) {
            Toast.info(this, "Open the Friends tab to respond.");
        }
    }
}
