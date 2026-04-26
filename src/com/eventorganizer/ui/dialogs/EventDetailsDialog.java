package com.eventorganizer.ui.dialogs;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.Event;
import com.eventorganizer.models.Invitation;
import com.eventorganizer.models.User;
import com.eventorganizer.models.enums.EventStatus;
import com.eventorganizer.models.enums.EventType;
import com.eventorganizer.models.enums.RSVPStatus;
import com.eventorganizer.ui.components.AsyncUI;
import com.eventorganizer.ui.components.Avatar;
// AppException already imported above for direct error handling on RSVP toggle
import com.eventorganizer.ui.components.Badge;
import com.eventorganizer.ui.components.RsvpDonut;
import com.eventorganizer.ui.components.SegmentedControl;
import com.eventorganizer.ui.components.SwingText;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.components.TrackedLabel;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.laf.AuroraButton;
import com.eventorganizer.ui.theme.Iconography;
import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.SoftBorder;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;
import com.eventorganizer.utils.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.time.format.TextStyle;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Two-column event details dialog. Left column hosts a date block, title,
 * badges, location, creator chip, description. Right column hosts the
 * {@link RsvpDonut} hero, legend, and RSVP/management actions.
 */
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
        super(parent, "Event details", 760, 600);

        User currentUser = controller.currentUser();
        boolean isCreator = currentUser != null && event.getCreatorId().equals(currentUser.getUserId());

        JPanel content = new JPanel(new BorderLayout(Spacing.XL, 0));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.M, Spacing.XL));

        content.add(buildLeft(controller, event), BorderLayout.WEST);
        content.add(buildRight(controller, event, currentUser, isCreator, onChanged, parent),
            BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);
        add(buildActions(controller, event, currentUser, isCreator, onChanged, parent),
            BorderLayout.SOUTH);
    }

    private JPanel buildLeft(UIController controller, Event event) {
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setPreferredSize(new Dimension(340, 0));

        // Date block
        JPanel dateBlock = new DateBlock(event);
        dateBlock.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(dateBlock);
        left.add(Box.createVerticalStrut(Spacing.L));

        // Title
        JLabel title = new JLabel(SwingText.plain(event.getName()));
        title.setFont(Typography.H1);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(title);

        // Badges
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.S, 0));
        badges.setOpaque(false);
        badges.setAlignmentX(Component.LEFT_ALIGNMENT);
        badges.add(new Badge(event.getType().toString(),
            event.getType() == EventType.PUBLIC ? Badge.Kind.ACCENT : Badge.Kind.ACCENT2));
        badges.add(new Badge(statusLabel(event), statusKind(event)));
        left.add(Box.createVerticalStrut(Spacing.S));
        left.add(badges);
        left.add(Box.createVerticalStrut(Spacing.L));

        // Meta rows (when, where, creator)
        left.add(metaRow("clock", "When",  DateUtil.format(event.getDateTime())));
        left.add(metaRow("pin",   "Where", event.getLocation() == null || event.getLocation().isEmpty()
            ? "—" : event.getLocation()));

        User creator = controller.lookupUser(event.getCreatorId());
        String creatorName = creator == null ? event.getCreatorId() : creator.getUsername();
        JPanel creatorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.S, 0));
        creatorRow.setOpaque(false);
        creatorRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        creatorRow.add(new Avatar(creatorName, Avatar.Size.S24));
        TrackedLabel hostedBy = new TrackedLabel("HOSTED BY", Typography.LABEL,
            Theme.TEXT_TERTIARY, 0.10f);
        creatorRow.add(hostedBy);
        JLabel host = new JLabel(creatorName);
        host.setFont(Typography.BODY_BOLD);
        host.setForeground(Theme.TEXT_PRIMARY);
        creatorRow.add(host);
        left.add(creatorRow);
        left.add(Box.createVerticalStrut(Spacing.L));

        // Description
        JTextArea descArea = new JTextArea(event.getDescription() == null ? "" : event.getDescription());
        descArea.setEditable(false);
        descArea.setFocusable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(Theme.BG_ELEVATED);
        descArea.setForeground(Theme.TEXT_PRIMARY);
        descArea.setFont(Typography.BODY);
        descArea.setBorder(SoftBorder.of(Radius.MD, Theme.BORDER_SUBTLE, 1,
            new Insets(Spacing.M, Spacing.M, Spacing.M, Spacing.M)));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createEmptyBorder());
        descScroll.setOpaque(false);
        descScroll.getViewport().setOpaque(false);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(descScroll);

        return left;
    }

    private JPanel buildRight(UIController controller, Event event, User currentUser,
                              boolean isCreator, Runnable onChanged, Component parent) {
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setPreferredSize(new Dimension(360, 0));
        right.setBorder(BorderFactory.createEmptyBorder(Spacing.M, 0, 0, 0));

        TrackedLabel rsvpLabel = new TrackedLabel("RSVP SUMMARY", Typography.LABEL,
            Theme.TEXT_SECONDARY, 0.10f);
        rsvpLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        right.add(rsvpLabel);
        right.add(Box.createVerticalStrut(Spacing.M));

        RsvpDonut donut = new RsvpDonut();
        donut.setAlignmentX(Component.CENTER_ALIGNMENT);
        donut.setMaximumSize(new Dimension(220, 220));
        donut.setPreferredSize(new Dimension(220, 220));
        donut.setCounts(loadCounts(controller, event));
        right.add(donut);
        right.add(Box.createVerticalStrut(Spacing.M));

        // Legend
        right.add(legendRow("ACCEPTED", Theme.SUCCESS,
            counts(controller, event).getOrDefault(RSVPStatus.ACCEPTED, 0L)));
        right.add(legendRow("MAYBE",    Theme.WARNING,
            counts(controller, event).getOrDefault(RSVPStatus.MAYBE, 0L)));
        right.add(legendRow("DECLINED", Theme.DANGER,
            counts(controller, event).getOrDefault(RSVPStatus.DECLINED, 0L)));
        right.add(legendRow("PENDING",  Theme.TEXT_TERTIARY,
            counts(controller, event).getOrDefault(RSVPStatus.PENDING, 0L)));

        // show the full invitee list with RSVP status to the event creator
        if (isCreator && !event.getInvitations().isEmpty()) {
            right.add(Box.createVerticalStrut(Spacing.L));
            TrackedLabel inviteesLabel = new TrackedLabel("INVITEES", Typography.LABEL,
                Theme.TEXT_SECONDARY, 0.10f);
            inviteesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            right.add(inviteesLabel);
            right.add(Box.createVerticalStrut(Spacing.S));
            right.add(buildInviteesList(controller, event));
        }

        // RSVP toggle for non-creator invitees
        if (!isCreator) {
            Invitation inv = event.getInvitationForUser(currentUser == null ? "" : currentUser.getUserId());

            // Self-join for public events the user wasn't explicitly invited to.
            if (inv == null
                && event.getType() == EventType.PUBLIC
                && event.getStatus() != EventStatus.CANCELLED
                && !event.isPast()
                && currentUser != null) {
                right.add(Box.createVerticalStrut(Spacing.L));
                TrackedLabel join = new TrackedLabel("JOIN", Typography.LABEL,
                    Theme.TEXT_SECONDARY, 0.10f);
                join.setAlignmentX(Component.LEFT_ALIGNMENT);
                right.add(join);
                right.add(Box.createVerticalStrut(Spacing.S));
                AuroraButton joinBtn = new AuroraButton("Join event", AuroraButton.Variant.DEFAULT);
                joinBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
                joinBtn.addActionListener(e -> {
                    try {
                        controller.joinPublicEvent(event.getEventId());
                        controller.respondRSVP(event.getEventId(), RSVPStatus.ACCEPTED);
                        Toast.success(parent, "You're going!");
                        donut.setCounts(loadCounts(controller, event));
                        if (onChanged != null) onChanged.run();
                        dispose();
                    } catch (AppException ex) {
                        Toast.error(getContentPane(), ex.getMessage());
                    }
                });
                JPanel joinWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                joinWrap.setOpaque(false);
                joinWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
                joinWrap.add(joinBtn);
                right.add(joinWrap);
            }

            if (inv != null && event.getStatus() != EventStatus.CANCELLED && !event.isPast()) {
                right.add(Box.createVerticalStrut(Spacing.L));
                TrackedLabel yourRsvp = new TrackedLabel("YOUR RSVP", Typography.LABEL,
                    Theme.TEXT_SECONDARY, 0.10f);
                yourRsvp.setAlignmentX(Component.LEFT_ALIGNMENT);
                right.add(yourRsvp);
                right.add(Box.createVerticalStrut(Spacing.S));

                SegmentedControl rsvpToggle = new SegmentedControl();
                rsvpToggle.addSegment("Accept");
                rsvpToggle.addSegment("Maybe");
                rsvpToggle.addSegment("Decline");
                int initial = (inv.getStatus() == RSVPStatus.ACCEPTED) ? 0
                    : (inv.getStatus() == RSVPStatus.MAYBE) ? 1
                    : (inv.getStatus() == RSVPStatus.DECLINED) ? 2 : -1;
                if (initial >= 0) rsvpToggle.setSelectedIndex(initial);

                JPanel rsvpWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                rsvpWrap.setOpaque(false);
                rsvpWrap.setAlignmentX(Component.LEFT_ALIGNMENT);
                rsvpWrap.add(rsvpToggle);
                right.add(rsvpWrap);

                rsvpToggle.onChange(idx -> {
                    RSVPStatus chosen = idx == 0 ? RSVPStatus.ACCEPTED
                        : idx == 1 ? RSVPStatus.MAYBE : RSVPStatus.DECLINED;
                    try {
                        controller.respondRSVP(event.getEventId(), chosen);
                        Toast.success(parent, "RSVP: " + chosen);
                        donut.setCounts(loadCounts(controller, event));
                        if (onChanged != null) onChanged.run();
                    } catch (AppException ex) {
                        Toast.error(getContentPane(), ex.getMessage());
                    }
                });
            }
        }

        return right;
    }

    private JPanel buildActions(UIController controller, Event event, User currentUser,
                                boolean isCreator, Runnable onChanged, Component parent) {
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(Spacing.M, Spacing.XL, Spacing.L, Spacing.XL));

        AuroraButton close = new AuroraButton("Close", AuroraButton.Variant.GHOST);
        close.addActionListener(e -> dispose());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, 0));
        right.setOpaque(false);

        if (isCreator) {
            AuroraButton invite = new AuroraButton("Invite", AuroraButton.Variant.OUTLINE);
            invite.setEnabled(event.getStatus() != EventStatus.CANCELLED && !event.isPast());
            invite.addActionListener(e ->
                InviteFriendsDialog.show(this, controller, event,
                    () -> { if (onChanged != null) onChanged.run(); dispose(); }));

            AuroraButton edit = new AuroraButton("Edit", AuroraButton.Variant.OUTLINE);
            edit.setEnabled(event.getStatus() != EventStatus.CANCELLED && !event.isPast());
            edit.addActionListener(e ->
                EditEventDialog.show(this, controller, event,
                    () -> { if (onChanged != null) onChanged.run(); dispose(); }));

            AuroraButton cancelEvent = new AuroraButton("Cancel event", AuroraButton.Variant.DANGER);
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
                        if (onChanged != null) onChanged.run();
                        dispose();
                    },
                    ex -> Toast.error(getContentPane(), ex.getMessage()));
            });

            right.add(invite);
            right.add(edit);
            right.add(cancelEvent);
        }

        right.add(close);

        actions.add(right, BorderLayout.EAST);
        getRootPane().setDefaultButton(close);
        return actions;
    }

    private static Map<RSVPStatus, Long> counts(UIController controller, Event event) {
        try { return controller.rsvpSummary(event.getEventId()); }
        catch (AppException e) { return new EnumMap<>(RSVPStatus.class); }
    }

    private static Map<RSVPStatus, Long> loadCounts(UIController controller, Event event) {
        return counts(controller, event);
    }

    private JPanel metaRow(String iconName, String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.S, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel icon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Iconography.paint((Graphics2D) g, iconName, 0, 0, 16f, Theme.TEXT_TERTIARY);
            }
        };
        icon.setOpaque(false);
        icon.setPreferredSize(new Dimension(18, 18));

        TrackedLabel l = new TrackedLabel(label.toUpperCase(), Typography.LABEL,
            Theme.TEXT_TERTIARY, 0.10f);
        JLabel v = new JLabel(value);
        v.setFont(Typography.BODY);
        v.setForeground(Theme.TEXT_PRIMARY);
        row.add(icon);
        row.add(l);
        row.add(v);

        return row;
    }

    /**
     * Scrollable invitee roster: avatar + username + RSVP status pill for each
     * invited user. Only shown to the event creator.
     */
    private JComponent buildInviteesList(UIController controller, com.eventorganizer.models.Event event) {
        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new javax.swing.BoxLayout(rows, javax.swing.BoxLayout.Y_AXIS));
        rows.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (Invitation inv : event.getInvitations()) {
            User invitee = controller.lookupUser(inv.getInviteeId());
            String name = invitee == null ? inv.getInviteeId() : invitee.getUsername();
            rows.add(buildInviteeRow(name, inv.getStatus()));
        }

        javax.swing.JScrollPane sp = new javax.swing.JScrollPane(rows);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(javax.swing.BorderFactory.createLineBorder(Theme.BORDER_SUBTLE));
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        sp.setPreferredSize(new Dimension(340, 140));
        return sp;
    }

    private JPanel buildInviteeRow(String username, RSVPStatus status) {
        JPanel row = new JPanel(new java.awt.BorderLayout(Spacing.S, 0));
        row.setOpaque(false);
        row.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE),
            javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.S, 0));
        left.setOpaque(false);
        left.add(new com.eventorganizer.ui.components.Avatar(username,
            com.eventorganizer.ui.components.Avatar.Size.S24));
        javax.swing.JLabel nameLabel = new javax.swing.JLabel(username);
        nameLabel.setFont(Typography.BODY);
        nameLabel.setForeground(Theme.TEXT_PRIMARY);
        left.add(nameLabel);

        com.eventorganizer.ui.components.Badge.Kind kind;
        String label;
        switch (status) {
            case ACCEPTED: kind = com.eventorganizer.ui.components.Badge.Kind.SUCCESS; label = "GOING";    break;
            case MAYBE:    kind = com.eventorganizer.ui.components.Badge.Kind.WARNING; label = "MAYBE";    break;
            case DECLINED: kind = com.eventorganizer.ui.components.Badge.Kind.DANGER;  label = "DECLINED"; break;
            case PENDING:
            default:       kind = com.eventorganizer.ui.components.Badge.Kind.DEFAULT; label = "PENDING";
        }
        row.add(left, java.awt.BorderLayout.WEST);
        row.add(new com.eventorganizer.ui.components.Badge(label, kind), java.awt.BorderLayout.EAST);
        return row;
    }

    private JPanel legendRow(String label, Color color, long count) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.S, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel swatch = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(color);
                    g2.fillRoundRect(0, 4, 12, 12, 3, 3);
                } finally {
                    g2.dispose();
                }
            }
        };
        swatch.setOpaque(false);
        swatch.setPreferredSize(new Dimension(12, 20));

        JLabel l = new JLabel(label);
        l.setFont(Typography.LABEL);
        l.setForeground(Theme.TEXT_SECONDARY);
        l.setPreferredSize(new Dimension(80, 20));

        JLabel c = new JLabel(String.valueOf(count));
        c.setFont(Typography.BODY_BOLD);
        c.setForeground(Theme.TEXT_PRIMARY);

        row.add(swatch);
        row.add(l);
        row.add(c);
        return row;
    }

    private static String statusLabel(Event e) {
        if (e.getStatus() == EventStatus.CANCELLED) return "CANCELLED";
        return e.isPast() ? "PAST" : "UPCOMING";
    }

    private static Badge.Kind statusKind(Event e) {
        if (e.getStatus() == EventStatus.CANCELLED) return Badge.Kind.DANGER;
        return e.isPast() ? Badge.Kind.DEFAULT : Badge.Kind.SUCCESS;
    }

    /** Big editorial date block: day on the left at 36 pt, month + year stacked right. */
    private static final class DateBlock extends JPanel {
        private final Event event;
        DateBlock(Event event) {
            this.event = event;
            setOpaque(false);
            setMaximumSize(new Dimension(220, 70));
            setPreferredSize(new Dimension(220, 70));
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

                int day = event.getDateTime() != null ? event.getDateTime().getDayOfMonth() : 0;
                String mo = event.getDateTime() == null ? "" :
                    event.getDateTime().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();
                String yr = event.getDateTime() == null ? "" :
                    String.valueOf(event.getDateTime().getYear());
                String time = event.getDateTime() == null ? "" :
                    String.format("%02d:%02d", event.getDateTime().getHour(), event.getDateTime().getMinute());

                g2.setFont(Typography.NUMERAL.deriveFont(36f));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                String dayStr = String.format("%02d", day);
                int dw = fm.stringWidth(dayStr);
                g2.setColor(Theme.TEXT_PRIMARY);
                g2.drawString(dayStr, 0, fm.getAscent());

                int textX = dw + Spacing.M;
                Typography.drawTracked(g2, mo, Typography.LABEL, 0.10f, textX, fm.getAscent() - 24);

                g2.setFont(Typography.H2);
                g2.setColor(Theme.TEXT_PRIMARY);
                java.awt.FontMetrics fm2 = g2.getFontMetrics();
                g2.drawString(yr, textX, fm.getAscent() - 8);

                g2.setFont(Typography.MONO);
                g2.setColor(Theme.ACCENT);
                g2.drawString(time, textX, fm.getAscent() + fm2.getAscent() - 6);
            } finally {
                g2.dispose();
            }
        }
    }
}
