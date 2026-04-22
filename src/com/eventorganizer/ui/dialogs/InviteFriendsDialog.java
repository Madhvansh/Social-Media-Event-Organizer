package com.eventorganizer.ui.dialogs;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.Event;
import com.eventorganizer.models.User;
import com.eventorganizer.models.dto.BatchInviteResult;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
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
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

public class InviteFriendsDialog {

    public static void show(Component parent, UIController controller, Event event, Runnable onInvited) {
        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(owner, "Invite Friends", JDialog.ModalityType.APPLICATION_MODAL);
        d.setLayout(new BorderLayout());

        DefaultListModel<String> model = new DefaultListModel<>();
        for (User u : controller.friends()) {
            if (!event.hasInvited(u.getUserId())) model.addElement(u.getUsername());
        }

        JLabel header = new JLabel("Select friends to invite to '" + event.getName() + "':");
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));

        JList<String> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setBackground(Theme.BG_ELEVATED);

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 16, 8, 16));

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> d.dispose());
        JButton invite = new JButton("Invite");
        invite.setMnemonic('I');
        invite.addActionListener(e -> {
            List<String> selected = new ArrayList<>(list.getSelectedValuesList());
            if (selected.isEmpty()) {
                Toast.warning(d.getContentPane(), "No friends selected.");
                return;
            }
            try {
                BatchInviteResult res = controller.inviteMany(event.getEventId(), selected);
                d.dispose();
                String summary = res.getInvitedCount() + " invited";
                if (res.getFailureCount() > 0) summary += ", " + res.getFailureCount() + " skipped";
                Toast.success(parent, summary);
                onInvited.run();
            } catch (AppException ex) {
                Toast.error(d.getContentPane(), ex.getMessage());
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.add(cancel); buttons.add(invite);
        d.getRootPane().setDefaultButton(invite);

        d.add(header, BorderLayout.NORTH);
        d.add(scroll, BorderLayout.CENTER);
        d.add(buttons, BorderLayout.SOUTH);
        d.setSize(400, 360);
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }
}
