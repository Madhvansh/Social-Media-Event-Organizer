package com.eventorganizer.ui.screens.panels;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.dto.UserProfileDTO;
import com.eventorganizer.ui.components.FormField;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.dialogs.ChangePasswordDialog;
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
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class ProfilePanel extends JPanel {

    private final UIController controller;
    private final JPanel body = new JPanel();

    public ProfilePanel(UIController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PRIMARY);

        JLabel title = new JLabel("Profile");
        title.setFont(Theme.FONT_DISPLAY);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(Spacing.L, Spacing.XL, Spacing.S, Spacing.XL));

        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Theme.BG_PRIMARY);
        body.setBorder(BorderFactory.createEmptyBorder(Spacing.S, Spacing.XL, Spacing.XL, Spacing.XL));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(Theme.BG_PRIMARY);

        add(title, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        try {
            UserProfileDTO p = controller.getProfile();
            body.removeAll();

            body.add(kv("Username", p.getUsername()));
            body.add(kv("User ID",  p.getUserId()));
            body.add(kv("Friends",  String.valueOf(p.getFriendCount())));
            body.add(kv("Events created", String.valueOf(p.getEventsCreated())));
            body.add(kv("Member since", DateUtil.format(p.getMemberSince())));
            body.add(Box.createVerticalStrut(Spacing.M));

            JTextField email = new JTextField(p.getEmail());
            JTextArea bio = new JTextArea(p.getBio() == null ? "" : p.getBio(), 3, 20);
            bio.setLineWrap(true); bio.setWrapStyleWord(true);

            FormField emailFF = new FormField("Email", email);
            FormField bioFF = new FormField("Bio", new javax.swing.JScrollPane(bio));

            body.add(emailFF);
            body.add(bioFF);

            JButton save = new JButton("Save Profile");
            save.setMnemonic('S');
            save.addActionListener(e -> {
                try {
                    controller.updateProfile(email.getText().trim(), bio.getText());
                    Toast.success(this, "Profile updated.");
                    refresh();
                } catch (AppException ex) {
                    emailFF.setError(ex.getMessage());
                    Toast.error(this, ex.getMessage());
                }
            });

            JButton changePw = new JButton("Change Password");
            changePw.setMnemonic('P');
            changePw.addActionListener(e -> ChangePasswordDialog.show(this, controller));

            JButton logoutOfAll = new JButton("Log out");
            logoutOfAll.addActionListener(e -> {
                controller.logout();
                Toast.info(this, "Logged out.");
            });

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.S, Spacing.S));
            actions.setOpaque(false);
            actions.setAlignmentX(Component.LEFT_ALIGNMENT);
            actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, actions.getPreferredSize().height));
            actions.add(save);
            actions.add(changePw);
            body.add(actions);
            body.add(Box.createVerticalGlue());

            body.revalidate();
            body.repaint();
        } catch (AppException ex) {
            Toast.error(this, ex.getMessage());
        }
    }

    private JPanel kv(String k, String v) {
        JPanel row = new JPanel(new BorderLayout(Spacing.M, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel kl = new JLabel(k);
        kl.setForeground(Theme.TEXT_MUTED);
        kl.setFont(Theme.FONT_SMALL);
        kl.setPreferredSize(new Dimension(120, 20));
        JLabel vl = new JLabel(v == null ? "-" : com.eventorganizer.ui.components.SwingText.plain(v));
        vl.setForeground(Theme.TEXT_PRIMARY);
        vl.setFont(Theme.FONT_BODY);
        row.add(kl, BorderLayout.WEST);
        row.add(vl, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return row;
    }
}
