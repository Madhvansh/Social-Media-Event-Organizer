package com.eventorganizer.ui.screens.panels;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.FriendRequest;
import com.eventorganizer.models.User;
import com.eventorganizer.ui.components.AsyncUI;
import com.eventorganizer.ui.components.EmptyState;
import com.eventorganizer.ui.components.FormField;
import com.eventorganizer.ui.components.FriendRow;
import com.eventorganizer.ui.components.FriendTile;
import com.eventorganizer.ui.components.SegmentedControl;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.dialogs.ConfirmDialog;
import com.eventorganizer.ui.laf.AuroraButton;
import com.eventorganizer.ui.laf.AuroraTextField;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Friends panel rebuilt around the {@link SegmentedControl} pattern. Three views:
 * <ul>
 *   <li>My Friends — grid of {@link FriendTile}s</li>
 *   <li>Incoming — vertical row list with quick accept/reject</li>
 *   <li>Send Request — center-pinned card with autocompleting input</li>
 * </ul>
 */
public class FriendsPanel extends JPanel {

    private final UIController controller;
    private final Runnable onChange;

    private final SegmentedControl segs = new SegmentedControl();
    private final CardLayout cards = new CardLayout();
    private final JPanel viewPanel = new JPanel(cards);

    private final JPanel friendsGrid = new JPanel();
    private final JPanel incomingList = new JPanel();
    private final JPanel sendCard;

    public FriendsPanel(UIController controller, Runnable onChange) {
        this.controller = controller;
        this.onChange = onChange;
        setOpaque(false);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Friends");
        title.setFont(Typography.DISPLAY);
        title.setForeground(Theme.TEXT_PRIMARY);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.M, Spacing.XL));
        header.add(title, BorderLayout.WEST);

        segs.addSegment("My Friends", 0);
        segs.addSegment("Incoming",   0);
        segs.addSegment("Send",       -1);
        segs.onChange(idx -> cards.show(viewPanel,
            idx == 0 ? "friends" : idx == 1 ? "incoming" : "send"));

        JPanel segsWrap = new JPanel(new BorderLayout());
        segsWrap.setOpaque(false);
        segsWrap.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.L, Spacing.XL));
        segsWrap.add(segs, BorderLayout.WEST);

        friendsGrid.setOpaque(false);
        friendsGrid.setLayout(new FlowLayout(FlowLayout.LEFT, Spacing.L, Spacing.L));

        incomingList.setOpaque(false);
        incomingList.setLayout(new BoxLayout(incomingList, BoxLayout.Y_AXIS));
        incomingList.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.XL, Spacing.XL));

        sendCard = buildSendRequestPanel();

        viewPanel.setOpaque(false);
        viewPanel.add(scrollOf(friendsGrid),  "friends");
        viewPanel.add(scrollOf(incomingList), "incoming");
        viewPanel.add(sendCard,               "send");

        JPanel headerWrap = new JPanel(new BorderLayout());
        headerWrap.setOpaque(false);
        headerWrap.add(header, BorderLayout.NORTH);
        headerWrap.add(segsWrap, BorderLayout.CENTER);

        add(headerWrap, BorderLayout.NORTH);
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
            List<User> friends = controller.friends();
            List<FriendRequest> incoming = controller.incomingReqs();
            renderFriends(friends);
            renderIncoming(incoming);
            segs.setCount(0, friends.size());
            segs.setCount(1, incoming.size());
        } catch (AppException ex) {
            Toast.error(this, ex.getMessage());
        }
    }

    private void renderFriends(List<User> friends) {
        friendsGrid.removeAll();
        if (friends.isEmpty()) {
            friendsGrid.add(new EmptyState("users", "No friends yet",
                "Head to 'Send' to find someone you know."));
        } else {
            for (User f : friends) {
                AuroraButton remove = new AuroraButton("Unfriend", AuroraButton.Variant.GHOST);
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
                            refresh(); if (onChange != null) onChange.run();
                        },
                        ex -> Toast.error(this, ex.getMessage()));
                });
                FriendTile tile = new FriendTile(f.getUsername(), null, null);
                JPanel cell = new JPanel(new BorderLayout());
                cell.setOpaque(false);
                cell.add(tile, BorderLayout.CENTER);
                JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                actions.setOpaque(false);
                actions.add(remove);
                cell.add(actions, BorderLayout.SOUTH);
                friendsGrid.add(cell);
            }
        }
        friendsGrid.revalidate();
        friendsGrid.repaint();
    }

    private void renderIncoming(List<FriendRequest> requests) {
        incomingList.removeAll();
        if (requests.isEmpty()) {
            incomingList.add(new EmptyState("envelope", "No pending requests",
                "When someone asks to connect, they'll show up here."));
        } else {
            for (FriendRequest r : requests) {
                User sender = controller.lookupUser(r.getSenderId());
                if (sender == null) continue;
                AuroraButton accept = new AuroraButton("Accept", AuroraButton.Variant.DEFAULT);
                AuroraButton reject = new AuroraButton("Reject", AuroraButton.Variant.GHOST);
                accept.addActionListener(e -> AsyncUI.run(accept,
                    () -> controller.acceptFriendRequest(r.getRequestId()),
                    () -> {
                        Toast.success(this, "Friend added.");
                        refresh(); if (onChange != null) onChange.run();
                    },
                    ex -> Toast.error(this, ex.getMessage())));
                reject.addActionListener(e -> AsyncUI.run(reject,
                    () -> controller.rejectFriendRequest(r.getRequestId()),
                    () -> { refresh(); if (onChange != null) onChange.run(); },
                    ex -> Toast.error(this, ex.getMessage())));
                FriendRow row = new FriendRow(sender.getUsername(), "wants to be friends")
                    .addAction(accept).addAction(reject);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                incomingList.add(row);
                incomingList.add(Box.createVerticalStrut(Spacing.S));
            }
        }
        incomingList.add(Box.createVerticalGlue());
        incomingList.revalidate();
        incomingList.repaint();
    }

    private JPanel buildSendRequestPanel() {
        JPanel outer = new JPanel(new java.awt.GridBagLayout());
        outer.setOpaque(false);
        outer.setBorder(BorderFactory.createEmptyBorder(Spacing.XXL, Spacing.XL, Spacing.XL, Spacing.XL));

        JPanel card = new JPanel();
        card.setOpaque(true);
        card.setBackground(Theme.BG_ELEVATED);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1, true),
            BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.XL, Spacing.XL)));

        JLabel header = new JLabel("Connect with someone");
        header.setFont(Typography.H1);
        header.setForeground(Theme.TEXT_PRIMARY);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Send a friend request by username.");
        sub.setFont(Typography.BODY);
        sub.setForeground(Theme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(Spacing.XS, 0, Spacing.L, 0));

        AuroraTextField usernameField = new AuroraTextField("e.g. alice");
        FormField ff = new FormField("USERNAME", usernameField);
        ff.setAlignmentX(Component.LEFT_ALIGNMENT);

        AuroraButton send = new AuroraButton("Send request", AuroraButton.Variant.DEFAULT);
        send.setMnemonic('S');
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
        hint.setFont(Typography.SMALL);
        hint.setForeground(Theme.TEXT_TERTIARY);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(header);
        card.add(sub);
        card.add(ff);
        card.add(Box.createVerticalStrut(Spacing.M));
        card.add(send);
        card.add(Box.createVerticalStrut(Spacing.M));
        card.add(hint);
        card.setMaximumSize(new java.awt.Dimension(440, 320));

        outer.add(card);
        return outer;
    }
}
