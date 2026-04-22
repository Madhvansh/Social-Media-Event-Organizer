package com.eventorganizer.ui.components;

import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class StatusBar extends JPanel {
    private final JLabel greeting = new JLabel();
    private final JLabel unread   = new JLabel();
    private final JButton profileBtn = new JButton("Profile");
    private final JButton logoutBtn  = new JButton("Logout");

    public StatusBar(UIController controller, Runnable onProfile, Runnable onLogout) {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_ELEVATED);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        setPreferredSize(new Dimension(0, 44));

        greeting.setForeground(Theme.TEXT_PRIMARY);
        greeting.setFont(Theme.FONT_TITLE);
        greeting.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));

        unread.setForeground(Theme.WARNING);
        unread.setFont(Theme.FONT_BODY);

        profileBtn.setMnemonic('P');
        logoutBtn.setMnemonic('L');

        profileBtn.addActionListener(e -> onProfile.run());
        logoutBtn.addActionListener(e -> onLogout.run());

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.add(unread);
        right.add(Box.createHorizontalStrut(12));
        right.add(profileBtn);
        right.add(logoutBtn);
        right.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));

        add(greeting, BorderLayout.WEST);
        add(right,    BorderLayout.EAST);

        refresh(controller);
    }

    public void refresh(UIController controller) {
        var u = controller.currentUser();
        greeting.setText(u == null ? "" : "Hi, " + u.getUsername());
        int count = controller.unreadCount();
        unread.setText(count == 0 ? "" : count + " unread");
    }
}
