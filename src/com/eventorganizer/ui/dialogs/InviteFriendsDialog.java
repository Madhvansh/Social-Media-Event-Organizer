package com.eventorganizer.ui.dialogs;

import com.eventorganizer.models.Event;
import com.eventorganizer.models.User;
import com.eventorganizer.models.dto.BatchInviteResult;
import com.eventorganizer.ui.components.AsyncUI;
import com.eventorganizer.ui.components.EmptyState;
import com.eventorganizer.ui.components.SwingText;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

public class InviteFriendsDialog {

    public static void show(Component parent, UIController controller, Event event, Runnable onInvited) {
        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(owner, "Invite Friends", JDialog.ModalityType.APPLICATION_MODAL);
        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(Theme.BG_PRIMARY);

        DefaultListModel<String> model = new DefaultListModel<>();
        for (User u : controller.friends()) {
            if (!event.hasInvited(u.getUserId())) model.addElement(u.getUsername());
        }

        JLabel header = new JLabel(SwingText.plain("Invite friends to '" + event.getName() + "'"));
        header.setFont(Theme.FONT_SUBTITLE);
        header.setForeground(Theme.TEXT_PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(Spacing.L, Spacing.L, Spacing.S, Spacing.L));

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> d.dispose());
        JButton invite = new JButton("Invite");
        invite.setMnemonic('I');
        invite.putClientProperty("JButton.buttonType", "default");

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, Spacing.S));
        buttons.setOpaque(false);
        buttons.setBorder(BorderFactory.createEmptyBorder(0, Spacing.L, Spacing.L, Spacing.L));
        buttons.add(cancel);
        buttons.add(invite);
        d.getRootPane().setDefaultButton(invite);

        d.add(header, BorderLayout.NORTH);
        d.add(buttons, BorderLayout.SOUTH);

        if (model.isEmpty()) {
            JPanel emptyWrap = new JPanel(new GridBagLayout());
            emptyWrap.setOpaque(false);
            emptyWrap.add(new EmptyState("✦", "Nothing to invite",
                "Every friend you have is already invited, or you haven't added friends yet."));
            d.add(emptyWrap, BorderLayout.CENTER);
            invite.setEnabled(false);
        } else {
            JList<String> list = new JList<>(model);
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            list.setBackground(Theme.BG_ELEVATED);
            list.setForeground(Theme.TEXT_PRIMARY);
            list.setFixedCellHeight(32);

            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(BorderFactory.createEmptyBorder(0, Spacing.L, Spacing.S, Spacing.L));
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            d.add(scroll, BorderLayout.CENTER);

            invite.addActionListener(e -> {
                List<String> selected = new ArrayList<>(list.getSelectedValuesList());
                if (selected.isEmpty()) {
                    Toast.warning(d.getContentPane(), "No friends selected.");
                    return;
                }
                AsyncUI.run(invite,
                    () -> controller.inviteMany(event.getEventId(), selected),
                    (BatchInviteResult res) -> {
                        d.dispose();
                        String summary = res.getInvitedCount() + " invited";
                        if (res.getFailureCount() > 0) summary += ", " + res.getFailureCount() + " skipped";
                        Toast.success(parent, summary);
                        onInvited.run();
                    },
                    ex -> Toast.error(d.getContentPane(), ex.getMessage()));
            });
        }

        d.setMinimumSize(new java.awt.Dimension(480, 520));
        d.setSize(480, 520);
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }
}
