package com.eventorganizer.ui.screens;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.ui.components.FormField;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.utils.PasswordHasher;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.function.Consumer;

public class AuthScreen extends JPanel {

    private final UIController controller;
    private final Consumer<Void> onAuthenticated;

    public AuthScreen(UIController controller, Consumer<Void> onAuthenticated) {
        this.controller = controller;
        this.onAuthenticated = onAuthenticated;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PRIMARY);

        JLabel title = new JLabel("Event Organizer", SwingConstants.CENTER);
        title.setFont(Theme.FONT_DISPLAY);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Login", buildLoginPanel());
        tabs.addTab("Register", buildRegisterPanel());

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(tabs, BorderLayout.CENTER);
        center.setBorder(BorderFactory.createEmptyBorder(0, 80, 80, 80));
        center.setPreferredSize(new Dimension(560, 440));

        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.add(title, BorderLayout.NORTH);
        outer.add(center, BorderLayout.CENTER);

        add(outer, BorderLayout.CENTER);
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        FormField usernameFF = new FormField("Username", usernameField);
        FormField passwordFF = new FormField("Password", passwordField);

        JButton loginBtn = new JButton("Log in");
        loginBtn.setMnemonic('L');
        loginBtn.addActionListener(e -> doLogin(usernameField, passwordField, usernameFF, passwordFF));

        panel.add(usernameFF);
        panel.add(passwordFF);
        panel.add(Box.createVerticalStrut(10));
        panel.add(loginBtn);
        panel.add(Box.createVerticalGlue());

        getRootPane();
        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));

        JTextField usernameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField pw1 = new JPasswordField();
        JPasswordField pw2 = new JPasswordField();

        FormField usernameFF = new FormField("Username", usernameField);
        FormField emailFF    = new FormField("Email", emailField);
        FormField pw1FF      = new FormField("Password", pw1);
        FormField pw2FF      = new FormField("Confirm password", pw2);

        JButton registerBtn = new JButton("Create account");
        registerBtn.setMnemonic('C');
        registerBtn.addActionListener(e ->
            doRegister(usernameField, emailField, pw1, pw2, usernameFF, emailFF, pw1FF, pw2FF));

        panel.add(usernameFF);
        panel.add(emailFF);
        panel.add(pw1FF);
        panel.add(pw2FF);
        panel.add(Box.createVerticalStrut(10));
        panel.add(registerBtn);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private void doLogin(JTextField username, JPasswordField pw,
                         FormField usernameFF, FormField pwFF) {
        usernameFF.clearError();
        pwFF.clearError();
        char[] raw = pw.getPassword();
        try {
            controller.login(username.getText().trim(), raw);
            Toast.success(this, "Welcome back!");
            onAuthenticated.accept(null);
        } catch (AppException ex) {
            pwFF.setError(ex.getMessage());
            Toast.error(this, ex.getMessage());
        } finally {
            Arrays.fill(raw, '\0');
            pw.setText("");
        }
    }

    private void doRegister(JTextField username, JTextField email,
                            JPasswordField pw1, JPasswordField pw2,
                            FormField uFF, FormField eFF, FormField p1FF, FormField p2FF) {
        uFF.clearError(); eFF.clearError(); p1FF.clearError(); p2FF.clearError();
        char[] a = pw1.getPassword();
        char[] b = pw2.getPassword();
        try {
            if (!passwordsMatch(a, b)) {
                p2FF.setError("Passwords do not match.");
                Toast.error(this, "Passwords do not match.");
                return;
            }
            controller.register(username.getText().trim(), email.getText().trim(), a);
            controller.login(username.getText().trim(), b);
            Toast.success(this, "Account created.");
            onAuthenticated.accept(null);
        } catch (AppException ex) {
            p1FF.setError(ex.getMessage());
            Toast.error(this, ex.getMessage());
        } finally {
            PasswordHasher.zero(a);
            PasswordHasher.zero(b);
            pw1.setText("");
            pw2.setText("");
        }
    }

    private boolean passwordsMatch(char[] a, char[] b) {
        if (a == null || b == null) return false;
        byte[] ab = toBytes(a);
        byte[] bb = toBytes(b);
        try {
            return MessageDigest.isEqual(ab, bb);
        } finally {
            Arrays.fill(ab, (byte) 0);
            Arrays.fill(bb, (byte) 0);
        }
    }

    private byte[] toBytes(char[] chars) {
        byte[] out = new byte[chars.length * 2];
        for (int i = 0; i < chars.length; i++) {
            out[i * 2]     = (byte) ((chars[i] >> 8) & 0xFF);
            out[i * 2 + 1] = (byte) (chars[i] & 0xFF);
        }
        return out;
    }
}
