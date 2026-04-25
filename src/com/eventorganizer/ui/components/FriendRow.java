package com.eventorganizer.ui.components;

import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.SoftBorder;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 * Horizontal friend-list row: {@link Avatar} (S32) on the left, name + optional
 * subtitle in the middle, action slot on the right.
 */
public class FriendRow extends JPanel {

    private final JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, 0));

    public FriendRow(String username, String subtitle) {
        setLayout(new BorderLayout(Spacing.M, 0));
        setOpaque(true);
        setBackground(Theme.BG_ELEVATED);
        setBorder(SoftBorder.of(Radius.MD, Theme.BORDER_SUBTLE, 1,
            new Insets(Spacing.M, Spacing.M, Spacing.M, Spacing.M)));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        Avatar avatar = new Avatar(username, Avatar.Size.S32);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new javax.swing.BoxLayout(text, javax.swing.BoxLayout.Y_AXIS));
        JLabel name = new JLabel(SwingText.plain(username));
        name.setFont(Typography.BODY_BOLD);
        name.setForeground(Theme.TEXT_PRIMARY);
        text.add(name);
        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel sub = new JLabel(subtitle);
            sub.setFont(Typography.SMALL);
            sub.setForeground(Theme.TEXT_SECONDARY);
            text.add(sub);
        }

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.M, 0));
        left.setOpaque(false);
        left.add(avatar);
        left.add(text);

        actions.setOpaque(false);

        add(left, BorderLayout.WEST);
        add(actions, BorderLayout.EAST);
    }

    public FriendRow addAction(java.awt.Component c) {
        actions.add(c);
        return this;
    }
}
