package com.eventorganizer.ui.screens.panels;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.FriendRequest;
import com.eventorganizer.models.User;
import com.eventorganizer.ui.components.FormField;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.dialogs.ConfirmDialog;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.List;

public class FriendsPanel extends JPanel {

    private final UIController controller;
    private final JPanel friendsList  = listPanel();
    private final JPanel incomingList = listPanel();
    private final Runnable onChange;

    public FriendsPanel(UIController controller, Runnable onChange) {
        this.controller = controller;
        this.onChange = onChange;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PRIMARY);

        JLabel title = new JLabel("Friends");
        title.setFont(Theme.FONT_DISPLAY);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My Friends",        new JScrollPane(friendsList));
        tabs.addTab("Incoming Requests", new JScrollPane(incomingList));
        tabs.addTab("Send Request",      buildSendRequestPanel());

        add(title, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        try {
            renderFriends(controller.friends());
            renderIncoming(controller.incomingReqs());
        } catch (AppException ex) {
            Toast.error(this, ex.getMessage());
        }
    }

    private void renderFriends(List<User> friends) {
        friendsList.removeAll();
        if (friends.isEmpty()) {
            friendsList.add(emptyState("No friends yet. Send a request!"));
        } else {
            for (User f : friends) {
                JPanel row = rowPanel();
                JLabel name = new JLabel(f.getUsername());
                name.setForeground(Theme.TEXT_PRIMARY);
                name.setFont(Theme.FONT_BODY);
                JButton remove = new JButton("Unfriend");
                remove.addActionListener(e -> {
                    boolean ok = ConfirmDialog.ask(this,
                        "Unfriend?",
                        "Remove " + f.getUsername() + " from your friends list?",
                        "Unfriend");
                    if (!ok) return;
                    try {
                        controller.removeFriend(f.getUsername());
                        Toast.success(this, "Removed " + f.getUsername());
                        refresh(); onChange.run();
                    } catch (AppException ex) { Toast.error(this, ex.getMessage()); }
                });
                row.add(name, BorderLayout.WEST);
                JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                right.setOpaque(false);
                right.add(remove);
                row.add(right, BorderLayout.EAST);
                friendsList.add(row);
                friendsList.add(Box.createVerticalStrut(6));
            }
        }
        friendsList.add(Box.createVerticalGlue());
        friendsList.revalidate();
        friendsList.repaint();
    }

    private void renderIncoming(List<FriendRequest> requests) {
        incomingList.removeAll();
        if (requests.isEmpty()) {
            incomingList.add(emptyState("No pending friend requests."));
        } else {
            for (FriendRequest r : requests) {
                User sender = controller.lookupUser(r.getSenderId());
                if (sender == null) continue;
                JPanel row = rowPanel();
                JLabel name = new JLabel(sender.getUsername() + " wants to be friends");
                name.setForeground(Theme.TEXT_PRIMARY);
                name.setFont(Theme.FONT_BODY);
                JButton accept = new JButton("Accept");
                JButton reject = new JButton("Reject");
                accept.addActionListener(e -> {
                    try {
                        controller.acceptFriendRequest(r.getRequestId());
                        Toast.success(this, "Friend added.");
                        refresh(); onChange.run();
                    } catch (AppException ex) { Toast.error(this, ex.getMessage()); }
                });
                reject.addActionListener(e -> {
                    try {
                        controller.rejectFriendRequest(r.getRequestId());
                        refresh(); onChange.run();
                    } catch (AppException ex) { Toast.error(this, ex.getMessage()); }
                });
                row.add(name, BorderLayout.WEST);
                JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                right.setOpaque(false);
                right.add(accept); right.add(reject);
                row.add(right, BorderLayout.EAST);
                incomingList.add(row);
                incomingList.add(Box.createVerticalStrut(6));
            }
        }
        incomingList.add(Box.createVerticalGlue());
        incomingList.revalidate();
        incomingList.repaint();
    }

    private JPanel buildSendRequestPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.BG_PRIMARY);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField usernameField = new JTextField();
        FormField ff = new FormField("Username", usernameField);
        JButton send = new JButton("Send Request");
        send.setMnemonic('S');
        send.addActionListener(e -> {
            try {
                controller.sendFriendRequest(usernameField.getText().trim());
                Toast.success(this, "Request sent.");
                usernameField.setText("");
                ff.clearError();
                refresh();
            } catch (AppException ex) {
                ff.setError(ex.getMessage());
                Toast.error(this, ex.getMessage());
            }
        });
        p.add(ff);
        p.add(send);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel rowPanel() {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Theme.BG_ELEVATED);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 48));
        return row;
    }

    private JPanel listPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.BG_PRIMARY);
        p.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
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
