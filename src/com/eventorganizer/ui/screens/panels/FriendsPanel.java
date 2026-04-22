package com.eventorganizer.ui.screens.panels;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.FriendRequest;
import com.eventorganizer.models.User;
import com.eventorganizer.ui.components.AsyncUI;
import com.eventorganizer.ui.components.EmptyState;
import com.eventorganizer.ui.components.FormField;
import com.eventorganizer.ui.components.FriendRow;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.dialogs.ConfirmDialog;
import com.eventorganizer.ui.theme.Spacing;
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
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

public class FriendsPanel extends JPanel {

    private final UIController controller;
    private final JPanel friendsList  = listPanel();
    private final JPanel incomingList = listPanel();
    private final JTabbedPane tabs = new JTabbedPane();
    private final Runnable onChange;

    public FriendsPanel(UIController controller, Runnable onChange) {
        this.controller = controller;
        this.onChange = onChange;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PRIMARY);

        JLabel title = new JLabel("Friends");
        title.setFont(Theme.FONT_DISPLAY);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(Spacing.L, Spacing.XL, Spacing.S, Spacing.XL));

        tabs.addTab("My Friends",        new JScrollPane(friendsList));
        tabs.addTab("Incoming Requests", new JScrollPane(incomingList));
        tabs.addTab("Send Request",      buildSendRequestPanel());

        add(title, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        try {
            List<User> friends = controller.friends();
            List<FriendRequest> incoming = controller.incomingReqs();
            renderFriends(friends);
            renderIncoming(incoming);
            tabs.setTitleAt(0, tabLabel("My Friends", friends.size()));
            tabs.setTitleAt(1, tabLabel("Incoming Requests", incoming.size()));
        } catch (AppException ex) {
            Toast.error(this, ex.getMessage());
        }
    }

    private static String tabLabel(String name, int count) {
        return count == 0 ? name : name + " (" + count + ")";
    }

    private void renderFriends(List<User> friends) {
        friendsList.removeAll();
        if (friends.isEmpty()) {
            friendsList.add(centered(new EmptyState("♥", "No friends yet",
                "Head to 'Send Request' to find someone you know.")));
        } else {
            for (User f : friends) {
                JButton remove = new JButton("Unfriend");
                remove.addActionListener(e -> {
                    boolean ok = ConfirmDialog.ask(this,
                        "Unfriend?",
                        "Remove " + f.getUsername() + " from your friends list?",
                        "Unfriend");
                    if (!ok) return;
                    AsyncUI.run(remove,
                        () -> controller.removeFriend(f.getUsername()),
                        () -> {
                            Toast.success(this, "Removed " + f.getUsername());
                            refresh(); onChange.run();
                        },
                        ex -> Toast.error(this, ex.getMessage()));
                });
                FriendRow row = new FriendRow(f.getUsername(), null).addAction(remove);
                friendsList.add(row);
                friendsList.add(Box.createVerticalStrut(Spacing.S));
            }
        }
        friendsList.add(Box.createVerticalGlue());
        friendsList.revalidate();
        friendsList.repaint();
    }

    private void renderIncoming(List<FriendRequest> requests) {
        incomingList.removeAll();
        if (requests.isEmpty()) {
            incomingList.add(centered(new EmptyState("✉", "No pending requests",
                "When someone asks to connect, they'll show up here.")));
        } else {
            for (FriendRequest r : requests) {
                User sender = controller.lookupUser(r.getSenderId());
                if (sender == null) continue;
                JButton accept = new JButton("Accept");
                accept.putClientProperty("JButton.buttonType", "default");
                JButton reject = new JButton("Reject");
                accept.addActionListener(e -> AsyncUI.run(accept,
                    () -> controller.acceptFriendRequest(r.getRequestId()),
                    () -> {
                        Toast.success(this, "Friend added.");
                        refresh(); onChange.run();
                    },
                    ex -> Toast.error(this, ex.getMessage())));
                reject.addActionListener(e -> AsyncUI.run(reject,
                    () -> controller.rejectFriendRequest(r.getRequestId()),
                    () -> { refresh(); onChange.run(); },
                    ex -> Toast.error(this, ex.getMessage())));
                FriendRow row = new FriendRow(sender.getUsername(), "wants to be friends")
                    .addAction(accept).addAction(reject);
                incomingList.add(row);
                incomingList.add(Box.createVerticalStrut(Spacing.S));
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
        p.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.XL, Spacing.XL));

        JTextField usernameField = new JTextField();
        FormField ff = new FormField("Username", usernameField);
        ff.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton send = new JButton("Send Request");
        send.setMnemonic('S');
        send.putClientProperty("JButton.buttonType", "default");
        send.setAlignmentX(Component.LEFT_ALIGNMENT);
        send.addActionListener(e -> {
            String target = usernameField.getText().trim();
            ff.clearError();
            AsyncUI.run(send,
                () -> controller.sendFriendRequest(target),
                () -> {
                    Toast.success(this, "Request sent.");
                    usernameField.setText("");
                    refresh();
                },
                ex -> {
                    ff.setError(ex.getMessage());
                    Toast.error(this, ex.getMessage());
                });
        });

        JLabel hint = new JLabel("Enter a username exactly as they typed it when they signed up.");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(ff);
        p.add(Box.createVerticalStrut(Spacing.M));
        p.add(send);
        p.add(Box.createVerticalStrut(Spacing.S));
        p.add(hint);
        p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel centered(Component c) {
        JPanel wrap = new JPanel(new java.awt.GridBagLayout());
        wrap.setOpaque(false);
        wrap.add(c);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        return wrap;
    }

    private JPanel listPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.BG_PRIMARY);
        p.setBorder(BorderFactory.createEmptyBorder(Spacing.M, Spacing.L, Spacing.M, Spacing.L));
        return p;
    }
}
