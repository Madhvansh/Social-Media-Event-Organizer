package com.eventorganizer.ui.dialogs;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.Event;
import com.eventorganizer.models.Invitation;
import com.eventorganizer.models.User;
import com.eventorganizer.models.enums.EventStatus;
import com.eventorganizer.models.enums.EventType;
import com.eventorganizer.models.enums.RSVPStatus;
import com.eventorganizer.ui.components.AsyncUI;
import com.eventorganizer.ui.components.Badge;
import com.eventorganizer.ui.components.SwingText;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.SoftBorder;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.utils.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class EventDetailsDialog extends AbstractAppDialog {

    public static void show(Component parent, UIController controller, String eventId, Runnable onChanged) {
        Event event;
        try {
            event = controller.viewEvent(eventId);
        } catch (AppException ex) {
            Toast.error(parent, ex.getMessage());
            return;
        }
        new EventDetailsDialog(parent, controller, event, onChanged).showCentered(parent);
    }

    private EventDetailsDialog(Component parent, UIController controller, Event event, Runnable onChanged) {
        super(parent, "Event: " + event.getName(), 560, 480);

        User currentUser = controller.currentUser();
        boolean isCreator = currentUser != null && event.getCreatorId().equals(currentUser.getUserId());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.L, Spacing.XL));

        JLabel heading = new JLabel(SwingText.plain(event.getName()));
        heading.setFont(Theme.FONT_TITLE);
        heading.setForeground(Theme.TEXT_PRIMARY);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.S, 0));
        badgeRow.setOpaque(false);
        badgeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        badgeRow.add(new Badge(event.getType().toString(),
            event.getType() == EventType.PUBLIC ? Badge.Kind.ACCENT : Badge.Kind.INFO));
        badgeRow.add(new Badge(statusLabel(event), statusKind(event)));

        content.add(heading);
        content.add(Box.createVerticalStrut(Spacing.S));
        content.add(badgeRow);
        content.add(Box.createVerticalStrut(Spacing.L));

        JPanel meta = new JPanel(new GridBagLayout());
        meta.setOpaque(false);
        meta.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.anchor = GridBagConstraints.LINE_START;
        gc.insets = new Insets(0, 0, Spacing.S, Spacing.L);
        addMetaRow(meta, gc, "When",   DateUtil.format(event.getDateTime()));
        addMetaRow(meta, gc, "Where",  event.getLocation());
        addMetaRow(meta, gc, "Status", event.getStatus().toString());
        content.add(meta);
        content.add(Box.createVerticalStrut(Spacing.L));

        JTextArea descArea = new JTextArea(event.getDescription() == null ? "" : event.getDescription());
        descArea.setEditable(false);
        descArea.setFocusable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(Theme.BG_ELEVATED);
        descArea.setForeground(Theme.TEXT_PRIMARY);
        descArea.setFont(Theme.FONT_BODY);
        descArea.setBorder(SoftBorder.of(Radius.MD, Theme.BORDER_SUBTLE, 1,
            new Insets(Spacing.M, Spacing.M, Spacing.M, Spacing.M)));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createEmptyBorder());
        descScroll.setOpaque(false);
        descScroll.getViewport().setOpaque(false);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(descScroll);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, Spacing.S));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.L, Spacing.XL));

        if (isCreator) {
            JLabel summary = new JLabel(summaryLine(controller, event.getEventId()));
            summary.setFont(Theme.FONT_SMALL);
            summary.setForeground(Theme.TEXT_MUTED);
            actions.add(summary);
            actions.add(Box.createHorizontalStrut(Spacing.M));

            JButton invite = new JButton("Invite");
            invite.setEnabled(event.getStatus() != EventStatus.CANCELLED && !event.isPast());
            invite.addActionListener(e ->
                InviteFriendsDialog.show(this, controller, event, () -> { onChanged.run(); dispose(); }));

            JButton edit = new JButton("Edit");
            edit.setEnabled(event.getStatus() != EventStatus.CANCELLED && !event.isPast());
            edit.addActionListener(e ->
                EditEventDialog.show(this, controller, event, () -> { onChanged.run(); dispose(); }));

            JButton cancelEvent = new JButton("Cancel event");
            cancelEvent.setForeground(Theme.DANGER);
            cancelEvent.setEnabled(event.getStatus() != EventStatus.CANCELLED);
            cancelEvent.addActionListener(e -> {
                boolean ok = ConfirmDialog.ask(getContentPane(),
                    "Cancel event?",
                    "This will cancel '" + event.getName() + "' and notify all invitees.",
                    "Cancel event");
                if (!ok) return;
                AsyncUI.run(cancelEvent,
                    () -> controller.cancelEvent(event.getEventId(), null),
                    () -> {
                        Toast.success(parent, "Event cancelled.");
                        onChanged.run();
                        dispose();
                    },
                    ex -> Toast.error(getContentPane(), ex.getMessage()));
            });

            actions.add(invite);
            actions.add(edit);
            actions.add(cancelEvent);
        } else {
            Invitation inv = event.getInvitationForUser(currentUser == null ? "" : currentUser.getUserId());
            if (inv != null && event.getStatus() != EventStatus.CANCELLED && !event.isPast()) {
                for (RSVPStatus s : new RSVPStatus[]{RSVPStatus.ACCEPTED, RSVPStatus.DECLINED, RSVPStatus.MAYBE}) {
                    JButton b = new JButton(s.name());
                    if (s == RSVPStatus.ACCEPTED) b.putClientProperty("JButton.buttonType", "default");
                    b.addActionListener(e -> AsyncUI.run(b,
                        () -> controller.respondRSVP(event.getEventId(), s),
                        () -> {
                            Toast.success(parent, "RSVP: " + s);
                            onChanged.run();
                            dispose();
                        },
                        ex -> Toast.error(getContentPane(), ex.getMessage())));
                    actions.add(b);
                }
            } else {
                JLabel info = new JLabel("This event is not currently open for RSVP.");
                info.setFont(Theme.FONT_SMALL);
                info.setForeground(Theme.TEXT_MUTED);
                actions.add(info);
            }
        }

        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        actions.add(close);

        add(content, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private static void addMetaRow(JPanel grid, GridBagConstraints gc, String label, String value) {
        JLabel k = new JLabel(label);
        k.setFont(Theme.FONT_SMALL);
        k.setForeground(Theme.TEXT_MUTED);
        JLabel v = new JLabel(value == null || value.isEmpty() ? "—" : value);
        v.setFont(Theme.FONT_BODY);
        v.setForeground(Theme.TEXT_PRIMARY);
        gc.gridx = 0; grid.add(k, gc);
        gc.gridx = 1; grid.add(v, gc);
        gc.gridy++;
    }

    private static String statusLabel(Event e) {
        if (e.getStatus() == EventStatus.CANCELLED) return "CANCELLED";
        return e.isPast() ? "PAST" : "UPCOMING";
    }

    private static Badge.Kind statusKind(Event e) {
        if (e.getStatus() == EventStatus.CANCELLED) return Badge.Kind.DANGER;
        return e.isPast() ? Badge.Kind.DEFAULT : Badge.Kind.SUCCESS;
    }

    private static String summaryLine(UIController controller, String eventId) {
        try {
            var counts = controller.rsvpSummary(eventId);
            return "ACCEPTED " + counts.getOrDefault(RSVPStatus.ACCEPTED, 0L)
                + "  •  DECLINED " + counts.getOrDefault(RSVPStatus.DECLINED, 0L)
                + "  •  MAYBE " + counts.getOrDefault(RSVPStatus.MAYBE, 0L)
                + "  •  PENDING " + counts.getOrDefault(RSVPStatus.PENDING, 0L);
        } catch (AppException e) {
            return "";
        }
    }
}
