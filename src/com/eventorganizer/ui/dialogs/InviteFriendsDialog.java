package com.eventorganizer.ui.dialogs;

import com.eventorganizer.models.Event;
import com.eventorganizer.models.User;
import com.eventorganizer.models.dto.BatchInviteResult;
import com.eventorganizer.ui.components.AsyncUI;
import com.eventorganizer.ui.components.Avatar;
import com.eventorganizer.ui.components.EmptyState;
import com.eventorganizer.ui.components.SwingText;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.fx.Animator;
import com.eventorganizer.ui.laf.AuroraButton;
import com.eventorganizer.ui.laf.AuroraTextField;
import com.eventorganizer.ui.theme.Easing;
import com.eventorganizer.ui.theme.Elevation;
import com.eventorganizer.ui.theme.Iconography;
import com.eventorganizer.ui.theme.Motion;
import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Invite friends dialog with a multi-select tile grid + sticky footer showing
 * the selected count. Filter input filters tiles in-place.
 */
public class InviteFriendsDialog {

    public static void show(Component parent, UIController controller, Event event, Runnable onInvited) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(owner, "Invite friends", JDialog.ModalityType.APPLICATION_MODAL);
        d.setLayout(new BorderLayout());
        d.getContentPane().setBackground(Theme.BG_PRIMARY);
        d.setMinimumSize(new Dimension(680, 600));

        JLabel header = new JLabel(SwingText.plain("Invite friends to '" + event.getName() + "'"));
        header.setFont(Typography.H1);
        header.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Select one or more friends to invite.");
        sub.setFont(Typography.BODY);
        sub.setForeground(Theme.TEXT_SECONDARY);

        AuroraTextField search = new AuroraTextField("Filter friends by name");
        search.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(Spacing.S, Spacing.M, Spacing.S, Spacing.M)));

        JPanel topStack = new JPanel();
        topStack.setOpaque(false);
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
        topStack.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.M, Spacing.XL));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(Spacing.XS, 0, Spacing.M, 0));
        search.setAlignmentX(Component.LEFT_ALIGNMENT);
        topStack.add(header);
        topStack.add(sub);
        topStack.add(search);

        // Build list of invitable friends
        List<User> invitable = new ArrayList<>();
        for (User u : controller.friends()) {
            if (!event.hasInvited(u.getUserId())) invitable.add(u);
        }

        Set<String> selected = new HashSet<>();
        JPanel grid = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.M, Spacing.M));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.M, Spacing.XL));

        JLabel selectedLabel = new JLabel("0 selected");
        selectedLabel.setFont(Typography.BODY_BOLD);
        selectedLabel.setForeground(Theme.TEXT_PRIMARY);

        AuroraButton cancel = new AuroraButton("Cancel", AuroraButton.Variant.GHOST);
        cancel.addActionListener(e -> d.dispose());
        AuroraButton invite = new AuroraButton("Invite", AuroraButton.Variant.DEFAULT);
        invite.setMnemonic('I');
        invite.setEnabled(false);
        d.getRootPane().setDefaultButton(invite);

        Runnable updateSelectedLabel = () -> {
            int n = selected.size();
            selectedLabel.setText(n + (n == 1 ? " friend selected" : " friends selected"));
            invite.setEnabled(n > 0);
        };

        if (invitable.isEmpty()) {
            JPanel emptyWrap = new JPanel(new GridBagLayout());
            emptyWrap.setOpaque(false);
            emptyWrap.add(new EmptyState("envelope", "Nothing to invite",
                "Every friend you have is already invited, or you haven't added friends yet."));
            d.add(topStack, BorderLayout.NORTH);
            d.add(emptyWrap, BorderLayout.CENTER);
        } else {
            List<SelectableFriendTile> tiles = new ArrayList<>();
            for (User u : invitable) {
                SelectableFriendTile tile = new SelectableFriendTile(u, () -> {
                    if (selected.contains(u.getUserId())) selected.remove(u.getUserId());
                    else selected.add(u.getUserId());
                    updateSelectedLabel.run();
                });
                tiles.add(tile);
                grid.add(tile);
            }

            search.getDocument().addDocumentListener(new DocumentListener() {
                private void apply() {
                    String q = search.getText().trim().toLowerCase();
                    for (SelectableFriendTile t : tiles) {
                        t.setVisible(q.isEmpty()
                            || t.username().toLowerCase().contains(q));
                    }
                    grid.revalidate();
                    grid.repaint();
                }
                @Override public void insertUpdate(DocumentEvent e) { apply(); }
                @Override public void removeUpdate(DocumentEvent e) { apply(); }
                @Override public void changedUpdate(DocumentEvent e) { apply(); }
            });

            JScrollPane scroll = new JScrollPane(grid);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getVerticalScrollBar().setUnitIncrement(24);

            d.add(topStack, BorderLayout.NORTH);
            d.add(scroll, BorderLayout.CENTER);

            invite.addActionListener(e -> {
                if (selected.isEmpty()) return;
                List<String> usernames = new ArrayList<>();
                for (User u : invitable) {
                    if (selected.contains(u.getUserId())) usernames.add(u.getUsername());
                }
                AsyncUI.run(invite,
                    () -> controller.inviteMany(event.getEventId(), usernames),
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

        // Sticky footer
        JPanel footer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setColor(Theme.BG_ELEVATED);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(Theme.BORDER_SUBTLE);
                    g2.fillRect(0, 0, getWidth(), 1);
                } finally {
                    g2.dispose();
                }
            }
        };
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(Spacing.M, Spacing.XL, Spacing.M, Spacing.XL));
        footer.add(selectedLabel, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, 0));
        actions.setOpaque(false);
        actions.add(cancel);
        actions.add(invite);
        footer.add(actions, BorderLayout.EAST);

        d.add(footer, BorderLayout.SOUTH);

        d.setSize(680, 600);
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }

    /** A friend tile with select-state painting. */
    private static final class SelectableFriendTile extends JComponent {
        private final User user;
        private final Runnable onToggle;
        private boolean selected;
        private float hoverT = 0f;
        private Animator.Handle handle;

        SelectableFriendTile(User u, Runnable onToggle) {
            this.user = u;
            this.onToggle = onToggle;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            Dimension d = new Dimension(150, 150);
            setPreferredSize(d);
            setMinimumSize(d);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { animate(1f); }
                @Override public void mouseExited(MouseEvent e)  { animate(0f); }
                @Override public void mouseClicked(MouseEvent e) {
                    selected = !selected;
                    SelectableFriendTile.this.onToggle.run();
                    repaint();
                }
            });
        }

        String username() { return user.getUsername(); }

        private void animate(float t) {
            if (Motion.REDUCED) { hoverT = t; repaint(); return; }
            if (handle != null) handle.cancel();
            final float start = hoverT;
            handle = Animator.tween(Motion.SHORT, Easing.EASE_OUT_CUBIC, p -> {
                hoverT = start + (t - start) * (float) p;
                repaint();
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

                int w = getWidth(), h = getHeight();
                int arc = Radius.LG;

                if (hoverT > 0.05f || selected) {
                    Elevation.paint(g2, 0, 0, w, h - 1, arc, Elevation.Tier.E1);
                }

                Color fill = selected ? Theme.ACCENT_SOFT : Theme.BG_ELEVATED;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                Color border = selected
                    ? Theme.ACCENT
                    : new Color(Theme.BORDER_SUBTLE.getRed(),
                        Theme.BORDER_SUBTLE.getGreen(), Theme.BORDER_SUBTLE.getBlue(),
                        160 + Math.round(95 * hoverT));
                g2.setColor(border);
                g2.setStroke(new java.awt.BasicStroke(selected ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

                // Avatar
                Avatar av = new Avatar(user.getUsername(), Avatar.Size.S48);
                av.setSize(48, 48);
                Graphics2D ag = (Graphics2D) g2.create((w - 48) / 2, 18, 48, 48);
                av.paint(ag);
                ag.dispose();

                // Username
                g2.setFont(Typography.BODY_BOLD);
                java.awt.FontMetrics fm = g2.getFontMetrics();
                String name = user.getUsername();
                int tw = fm.stringWidth(name);
                if (tw > w - 16) {
                    while (name.length() > 0 && fm.stringWidth(name + "…") > w - 16) {
                        name = name.substring(0, name.length() - 1);
                    }
                    name = name + "…";
                    tw = fm.stringWidth(name);
                }
                g2.setColor(Theme.TEXT_PRIMARY);
                g2.drawString(name, (w - tw) / 2, 90);

                // Selected check
                if (selected) {
                    int cx = w - 24, cy = 12;
                    g2.setColor(Theme.ACCENT);
                    g2.fillOval(cx, cy, 16, 16);
                    Iconography.paint(g2, "check", cx + 1, cy + 1, 14f, new Color(0x1B1612));
                } else {
                    int cx = w - 24, cy = 12;
                    g2.setColor(Theme.BG_OVERLAY);
                    g2.fillOval(cx, cy, 16, 16);
                    g2.setColor(Theme.BORDER);
                    g2.drawOval(cx, cy, 15, 15);
                }
            } finally {
                g2.dispose();
            }
        }
    }
}
