package com.eventorganizer.ui.screens.panels;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.EventUpdateNotification;
import com.eventorganizer.models.FriendRequestNotification;
import com.eventorganizer.models.InvitationNotification;
import com.eventorganizer.models.Notification;
import com.eventorganizer.ui.components.NotificationRow;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.dialogs.EventDetailsDialog;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class NotificationsPanel extends JPanel {

    private final UIController controller;
    private final DefaultListModel<Notification> model = new DefaultListModel<>();
    private final JList<Notification> list;
    private final Runnable onChange;

    public NotificationsPanel(UIController controller, Runnable onChange) {
        this.controller = controller;
        this.onChange = onChange;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PRIMARY);

        JLabel title = new JLabel("Notifications");
        title.setFont(Theme.FONT_DISPLAY);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(16, 20, 4, 20));

        JButton markAll = new JButton("Mark all as read");
        markAll.setMnemonic('M');
        markAll.addActionListener(e -> {
            try { controller.markAllRead(); refresh(); onChange.run(); }
            catch (AppException ex) { Toast.error(this, ex.getMessage()); }
        });

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);
        right.add(markAll);
        right.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 16));
        top.add(right, BorderLayout.EAST);

        list = new JList<>(model);
        list.setCellRenderer(new NotificationRow());
        list.setBackground(Theme.BG_PRIMARY);
        list.setFixedCellHeight(56);
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
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        scroll.getViewport().setPreferredSize(new Dimension(0, 0));

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        try {
            List<Notification> all = controller.notifications();
            model.clear();
            for (Notification n : all) model.addElement(n);
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
}
