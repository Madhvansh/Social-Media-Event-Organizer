package com.eventorganizer.ui.dialogs;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.Event;
import com.eventorganizer.models.Invitation;
import com.eventorganizer.models.User;
import com.eventorganizer.models.enums.EventStatus;
import com.eventorganizer.models.enums.RSVPStatus;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.utils.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;

public class EventDetailsDialog {

    public static void show(Component parent, UIController controller, String eventId, Runnable onChanged) {
        Event event;
        try {
            event = controller.viewEvent(eventId);
        } catch (AppException ex) {
            Toast.error(parent, ex.getMessage());
            return;
        }
        User currentUser = controller.currentUser();
        boolean isCreator = currentUser != null && event.getCreatorId().equals(currentUser.getUserId());

        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(owner, "Event: " + event.getName(), JDialog.ModalityType.APPLICATION_MODAL);
        d.setLayout(new BorderLayout(0, 8));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Theme.BG_PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));

        header.add(labeled("Name", event.getName()));
        header.add(labeled("When", DateUtil.format(event.getDateTime())));
        header.add(labeled("Where", event.getLocation()));
        header.add(labeled("Type", event.getType().toString()));
        header.add(labeled("Status", event.getStatus().toString()));
        header.add(Box.createVerticalStrut(6));

        JTextArea descArea = new JTextArea(event.getDescription());
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setRows(3);
        descArea.setBackground(Theme.BG_ELEVATED);
        descArea.setForeground(Theme.TEXT_PRIMARY);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        header.add(descScroll);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        if (isCreator) {
            JLabel summary = new JLabel(summaryLine(controller, eventId));
            summary.setFont(Theme.FONT_SMALL);
            summary.setForeground(Theme.TEXT_MUTED);
            actions.add(summary);

            JButton invite = new JButton("Invite");
            invite.setEnabled(event.getStatus() != EventStatus.CANCELLED && !event.isPast());
            invite.addActionListener(e ->
                InviteFriendsDialog.show(parent, controller, event, () -> { onChanged.run(); d.dispose(); }));

            JButton edit = new JButton("Edit");
            edit.setEnabled(event.getStatus() != EventStatus.CANCELLED && !event.isPast());
            edit.addActionListener(e ->
                EditEventDialog.show(parent, controller, event, () -> { onChanged.run(); d.dispose(); }));

            JButton cancelEvent = new JButton("Cancel event");
            cancelEvent.setEnabled(event.getStatus() != EventStatus.CANCELLED);
            cancelEvent.addActionListener(e -> {
                boolean ok = ConfirmDialog.ask(d.getContentPane(),
                    "Cancel event?",
                    "This will cancel '" + event.getName() + "' and notify all invitees.",
                    "Cancel event");
                if (!ok) return;
                try {
                    controller.cancelEvent(event.getEventId(), null);
                    Toast.success(parent, "Event cancelled.");
                    onChanged.run();
                    d.dispose();
                } catch (AppException ex) {
                    Toast.error(d.getContentPane(), ex.getMessage());
                }
            });

            actions.add(invite);
            actions.add(edit);
            actions.add(cancelEvent);
        } else {
            Invitation inv = event.getInvitationForUser(currentUser == null ? "" : currentUser.getUserId());
            if (inv != null && event.getStatus() != EventStatus.CANCELLED && !event.isPast()) {
                for (RSVPStatus s : new RSVPStatus[]{RSVPStatus.ACCEPTED, RSVPStatus.DECLINED, RSVPStatus.MAYBE}) {
                    JButton b = new JButton(s.name());
                    b.addActionListener(e -> {
                        try {
                            controller.respondRSVP(event.getEventId(), s);
                            Toast.success(parent, "RSVP: " + s);
                            onChanged.run();
                            d.dispose();
                        } catch (AppException ex) {
                            Toast.error(d.getContentPane(), ex.getMessage());
                        }
                    });
                    actions.add(b);
                }
            } else {
                JLabel info = new JLabel("This event is not currently open for RSVP.");
                info.setForeground(Theme.TEXT_MUTED);
                actions.add(info);
            }
        }

        JButton close = new JButton("Close");
        close.addActionListener(e -> d.dispose());
        actions.add(close);

        d.add(header, BorderLayout.CENTER);
        d.add(actions, BorderLayout.SOUTH);
        d.setSize(520, 420);
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }

    private static String summaryLine(UIController controller, String eventId) {
        try {
            var counts = controller.rsvpSummary(eventId);
            return "ACCEPTED " + counts.getOrDefault(RSVPStatus.ACCEPTED, 0L)
                + " | DECLINED " + counts.getOrDefault(RSVPStatus.DECLINED, 0L)
                + " | MAYBE " + counts.getOrDefault(RSVPStatus.MAYBE, 0L)
                + " | PENDING " + counts.getOrDefault(RSVPStatus.PENDING, 0L);
        } catch (AppException e) {
            return "";
        }
    }

    private static JPanel labeled(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        JLabel k = new JLabel(label);
        k.setForeground(Theme.TEXT_MUTED);
        k.setFont(Theme.FONT_SMALL);
        k.setPreferredSize(new java.awt.Dimension(70, 18));
        JLabel v = new JLabel(value == null ? "-" : value);
        v.setForeground(Theme.TEXT_PRIMARY);
        v.setFont(Theme.FONT_BODY);
        row.add(k, BorderLayout.WEST);
        row.add(v, BorderLayout.CENTER);
        return row;
    }
}
