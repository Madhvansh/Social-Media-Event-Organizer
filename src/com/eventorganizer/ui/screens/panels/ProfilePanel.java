package com.eventorganizer.ui.screens.panels;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.dto.UserProfileDTO;
import com.eventorganizer.ui.components.FormField;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.dialogs.ChangePasswordDialog;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.utils.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Component;
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
        title.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));

        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(Theme.BG_PRIMARY);
        body.setBorder(BorderFactory.createEmptyBorder(8, 20, 20, 20));

        add(title, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

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
            body.add(Box.createVerticalStrut(12));

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

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
            actions.setOpaque(false);
            actions.setAlignmentX(Component.LEFT_ALIGNMENT);
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
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel kl = new JLabel(k);
        kl.setForeground(Theme.TEXT_MUTED);
        kl.setFont(Theme.FONT_SMALL);
        kl.setPreferredSize(new java.awt.Dimension(120, 20));
        JLabel vl = new JLabel(v == null ? "-" : v);
        vl.setForeground(Theme.TEXT_PRIMARY);
        vl.setFont(Theme.FONT_BODY);
        row.add(kl, BorderLayout.WEST);
        row.add(vl, BorderLayout.CENTER);
        row.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 24));
        return row;
    }
}
