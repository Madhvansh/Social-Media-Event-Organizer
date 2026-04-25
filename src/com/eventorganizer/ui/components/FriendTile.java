package com.eventorganizer.ui.components;

import com.eventorganizer.ui.fx.Animator;
import com.eventorganizer.ui.theme.Easing;
import com.eventorganizer.ui.theme.Elevation;
import com.eventorganizer.ui.theme.Gradient;
import com.eventorganizer.ui.theme.Motion;
import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 140-wide friend tile for a grid: 48 px avatar on top, username + subtitle
 * below. Hover fades in a subtle accent border and lifts with Elevation E1.
 *
 * <p>Caller may pass a primary action handler invoked on click.
 */
public final class FriendTile extends JPanel {

    private float hoverT = 0f;
    private Animator.Handle handle;

    public FriendTile(String username, String subtitle, Runnable onClick) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(Spacing.L, Spacing.M, Spacing.L, Spacing.M));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Avatar avatar = new Avatar(username, Avatar.Size.S48);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel(SwingText.plain(username));
        name.setFont(Typography.BODY_BOLD);
        name.setForeground(Theme.TEXT_PRIMARY);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(avatar);
        add(Box.createVerticalStrut(Spacing.S));
        add(name);
        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel sub = new JLabel(subtitle);
            sub.setFont(Typography.SMALL);
            sub.setForeground(Theme.TEXT_SECONDARY);
            sub.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(Box.createVerticalStrut(2));
            add(sub);
        }

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { animate(1f); }
            @Override public void mouseExited(MouseEvent e)  { animate(0f); }
            @Override public void mouseClicked(MouseEvent e) {
                if (onClick != null) onClick.run();
            }
        });
    }

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
    public Dimension getPreferredSize() { return new Dimension(140, 152); }
    @Override public Dimension getMinimumSize() { return getPreferredSize(); }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth(), h = getHeight();
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = Radius.LG;
            if (hoverT > 0.05f) {
                Elevation.paint(g2, 0, 0, w, h - 1, arc, Elevation.Tier.E1);
            }
            g2.setPaint(Gradient.elevatedWash(w, h));
            g2.fillRoundRect(0, 0, w, h, arc, arc);
            Color border = hoverT > 0.05f
                ? new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(),
                    Math.round(255 * (0.25f + 0.65f * hoverT)))
                : Theme.BORDER_SUBTLE;
            g2.setColor(border);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
        } finally {
            g2.dispose();
        }
    }
}
