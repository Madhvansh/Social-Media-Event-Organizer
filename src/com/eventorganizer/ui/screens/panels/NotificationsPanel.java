package com.eventorganizer.ui.screens.panels;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.EventUpdateNotification;
import com.eventorganizer.models.FriendRequestNotification;
import com.eventorganizer.models.InvitationNotification;
import com.eventorganizer.models.Notification;
import com.eventorganizer.ui.components.AsyncUI;
import com.eventorganizer.ui.components.EmptyState;
import com.eventorganizer.ui.components.NotificationRow;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.dialogs.EventDetailsDialog;
import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.SoftBorder;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class NotificationsPanel extends JPanel {

    private static final String CARD_LIST  = "list";
    private static final String CARD_EMPTY = "empty";

    private final UIController controller;
    private final DefaultListModel<Notification> model = new DefaultListModel<>();
    private final JList<Notification> list;
    private final Runnable onChange;
    private final CardLayout bodyLayout = new CardLayout();
    private final JPanel body = new JPanel(bodyLayout);
    private final Map<String, FilterPill> pills = new LinkedHashMap<>();
    private Predicate<Notification> activeFilter = n -> true;
    private String activeKey = "ALL";

    public NotificationsPanel(UIController controller, Runnable onChange) {
        this.controller = controller;
        this.onChange = onChange;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PRIMARY);

        JLabel title = new JLabel("Notifications");
        title.setFont(Theme.FONT_DISPLAY);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(Spacing.L, Spacing.XL, Spacing.XS, Spacing.XL));

        JButton markAll = new JButton("Mark all as read");
        markAll.setMnemonic('M');
        markAll.addActionListener(e -> AsyncUI.run(markAll,
            () -> controller.markAllRead(),
            () -> { refresh(); onChange.run(); },
            ex -> Toast.error(this, ex.getMessage())));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, 0));
        right.setOpaque(false);
        right.add(markAll);
        right.setBorder(BorderFactory.createEmptyBorder(Spacing.S, 0, 0, Spacing.L));
        top.add(right, BorderLayout.EAST);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.S, 0));
        filterBar.setOpaque(false);
        filterBar.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.M, Spacing.XL));
        addPill(filterBar, "ALL",     "All",             n -> true);
        addPill(filterBar, "UNREAD",  "Unread",          n -> !n.isRead());
        addPill(filterBar, "INVITE",  "Invitations",     n -> n instanceof InvitationNotification);
        addPill(filterBar, "RSVP",    "RSVPs",           n -> n instanceof com.eventorganizer.models.RSVPNotification);
        addPill(filterBar, "FRIEND",  "Friend requests", n -> n instanceof FriendRequestNotification);
        addPill(filterBar, "UPDATE",  "Event updates",   n -> n instanceof EventUpdateNotification);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(top, BorderLayout.NORTH);
        header.add(filterBar, BorderLayout.SOUTH);

        list = new JList<>(model);
        list.setCellRenderer(new NotificationRow());
        list.setBackground(Theme.BG_PRIMARY);
        list.setFixedCellHeight(60);
        list.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2) return;
                Notification n = list.getSelectedValue();
                if (n == null) return;
                n.markAsRead();
                onChange.run();
                deepLink(n);
            }
        });

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder(Spacing.S, Spacing.L, Spacing.L, Spacing.L));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setPreferredSize(new Dimension(0, 0));

        JPanel emptyWrap = new JPanel(new java.awt.GridBagLayout());
        emptyWrap.setOpaque(false);
        emptyWrap.add(new EmptyState("✓", "You're all caught up",
            "New invitations, RSVPs, and updates will appear here."));

        body.setOpaque(false);
        body.add(scroll, CARD_LIST);
        body.add(emptyWrap, CARD_EMPTY);

        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        refresh();
    }

    private void addPill(JPanel parent, String key, String label, Predicate<Notification> filter) {
        FilterPill p = new FilterPill(label, "ALL".equals(key));
        p.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                activeKey = key;
                activeFilter = filter;
                for (FilterPill other : pills.values()) other.setSelected(false);
                p.setSelected(true);
                refresh();
            }
        });
        pills.put(key, p);
        parent.add(p);
    }

    public void refresh() {
        try {
            List<Notification> all = controller.notifications();
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
            EventDetailsDialog.show(this, controller, eventId, () -> { refresh(); onChange.run(); });
        } else if (n instanceof FriendRequestNotification) {
            Toast.info(this, "Open the Friends tab to respond.");
        }
    }

    private static final class FilterPill extends JPanel {
        private final JLabel label;
        private boolean selected;

        FilterPill(String text, boolean selected) {
            this.selected = selected;
            setLayout(new BorderLayout());
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(SoftBorder.of(Radius.LG, Theme.BORDER, 1,
                new Insets(Spacing.XS, Spacing.M, Spacing.XS, Spacing.M)));
            label = new JLabel(text);
            label.setFont(Theme.FONT_SMALL);
            label.setForeground(selected ? Theme.ACCENT : Theme.TEXT_MUTED);
            add(label, BorderLayout.CENTER);
        }

        void setSelected(boolean s) {
            this.selected = s;
            label.setForeground(s ? Theme.ACCENT : Theme.TEXT_MUTED);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = selected ? Theme.ACCENT_SOFT : Theme.BG_ELEVATED;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Radius.LG * 2, Radius.LG * 2);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
