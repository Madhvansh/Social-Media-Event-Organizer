package com.eventorganizer.ui.components;

import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.laf.AuroraButton;
import com.eventorganizer.ui.theme.Iconography;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Top "Mast" shell element. Left: small wordmark + welcome line. Right:
 * unread-badge chip, avatar chip (opens Profile on click), logout button.
 *
 * <p>Public API preserved from the original {@code StatusBar}: same constructor
 * and {@link #refresh} method so DashboardScreen remains untouched.
 */
public class StatusBar extends JPanel {

    private final JLabel greeting = new JLabel();
    private final UnreadPill unreadPill = new UnreadPill();
    private final AvatarChip avatarChip;
    private final AuroraButton logoutBtn;

    public StatusBar(UIController controller, Runnable onProfile, Runnable onLogout) {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Theme.BG_ELEVATED);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(8, Spacing.XL, 8, Spacing.XL)));
        setPreferredSize(new Dimension(0, 56));

        greeting.setForeground(Theme.TEXT_SECONDARY);
        greeting.setFont(Typography.BODY);

        avatarChip = new AvatarChip(controller, onProfile);
        logoutBtn = new AuroraButton("Log out", AuroraButton.Variant.GHOST);
        logoutBtn.setMnemonic('L');
        logoutBtn.addActionListener(e -> onLogout.run());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.M, 0));
        right.setOpaque(false);
        right.add(unreadPill);
        right.add(avatarChip);
        right.add(logoutBtn);

        add(greeting, BorderLayout.WEST);
        add(right,    BorderLayout.EAST);

        refresh(controller);
    }

    public void refresh(UIController controller) {
        var u = controller.currentUser();
        greeting.setText(u == null ? "" : "Welcome back, " + u.getUsername() + ".");
        avatarChip.refresh(controller);
        int count = controller.unreadCount();
        unreadPill.setCount(count);
    }

    /** Small pill showing unread count with a bell glyph; hidden when zero. */
    private static final class UnreadPill extends JComponent {
        private int count = 0;
        UnreadPill() {
            Dimension d = new Dimension(72, 30);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
            setVisible(false);
        }
        void setCount(int c) {
            this.count = c;
            setVisible(c > 0);
            repaint();
        }
        @Override
        protected void paintComponent(Graphics g) {
            if (count <= 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
                g2.setColor(Theme.ACCENT_SOFT);
                g2.fillRoundRect(0, 4, getWidth(), getHeight() - 8, 999, 999);
                g2.setColor(Theme.ACCENT);
                g2.drawRoundRect(0, 4, getWidth() - 1, getHeight() - 9, 999, 999);
                Iconography.paint(g2, "bell", 8, 9, 14f, Theme.ACCENT);
                g2.setFont(Typography.BODY_BOLD);
                java.awt.FontMetrics fm = g2.getFontMetrics();
                String s = count >= 100 ? "99+" : String.valueOf(count);
                int x = 26;
                int y = 4 + (getHeight() - 8 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(s, x, y);
            } finally {
                g2.dispose();
            }
        }
    }

    /** Clickable avatar chip: avatar + username with hover background. */
    private static final class AvatarChip extends JPanel {
        private final Avatar[] avatarHolder = new Avatar[1];
        private final JLabel name = new JLabel();
        private boolean hover;
        AvatarChip(UIController controller, Runnable onProfile) {
            setLayout(new FlowLayout(FlowLayout.LEFT, Spacing.S, 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(4, Spacing.S, 4, Spacing.M));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            name.setFont(Typography.BODY_BOLD);
            name.setForeground(Theme.TEXT_PRIMARY);
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) {
                    if (onProfile != null) onProfile.run();
                }
            });
        }
        void refresh(UIController controller) {
            removeAll();
            var u = controller.currentUser();
            String uname = u == null ? "?" : u.getUsername();
            avatarHolder[0] = new Avatar(uname, Avatar.Size.S32);
            add(avatarHolder[0]);
            name.setText(uname);
            add(name);
            revalidate();
            repaint();
        }
        @Override
        protected void paintComponent(Graphics g) {
            if (!hover) return;
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(Theme.BG_OVERLAY.getRed(), Theme.BG_OVERLAY.getGreen(),
                    Theme.BG_OVERLAY.getBlue(), 180));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 999, 999);
            } finally {
                g2.dispose();
            }
        }
    }
}
