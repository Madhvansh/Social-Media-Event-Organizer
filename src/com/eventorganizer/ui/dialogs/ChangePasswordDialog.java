package com.eventorganizer.ui.dialogs;

import com.eventorganizer.ui.components.AsyncUI;
import com.eventorganizer.ui.components.FormField;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.utils.PasswordHasher;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.security.MessageDigest;
import java.util.Arrays;

public class ChangePasswordDialog extends AbstractAppDialog {

    public static void show(Component parent, UIController controller) {
        new ChangePasswordDialog(parent, controller).showCentered(parent);
    }

    private ChangePasswordDialog(Component parent, UIController controller) {
        super(parent, "Change Password", 400, 360);

        JPasswordField oldPw = new JPasswordField();
        JPasswordField newPw = new JPasswordField();
        JPasswordField confirmPw = new JPasswordField();

        FormField oldFF  = new FormField("Current password", oldPw);
        FormField newFF  = new FormField("New password", newPw);
        FormField confFF = new FormField("Confirm new password", confirmPw);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Theme.BG_PRIMARY);
        form.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.L, Spacing.XL));
        form.add(oldFF);
        form.add(newFF);
        form.add(confFF);

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton save = new JButton("Change");
        save.setMnemonic('H');
        save.putClientProperty("JButton.buttonType", "default");
        save.addActionListener(e -> {
            char[] a = oldPw.getPassword();
            char[] b = newPw.getPassword();
            char[] c = confirmPw.getPassword();
            if (!constantEquals(b, c)) {
                confFF.setError("Passwords do not match.");
                Toast.error(getContentPane(), "Passwords do not match.");
                PasswordHasher.zero(a); PasswordHasher.zero(b); PasswordHasher.zero(c);
                return;
            }
            AsyncUI.run(save,
                () -> {
                    try { controller.changePassword(a, b); }
                    finally {
                        PasswordHasher.zero(a);
                        PasswordHasher.zero(b);
                        PasswordHasher.zero(c);
                    }
                },
                () -> {
                    oldPw.setText(""); newPw.setText(""); confirmPw.setText("");
                    Toast.success(parent, "Password changed.");
                    dispose();
                },
                ex -> {
                    oldPw.setText(""); newPw.setText(""); confirmPw.setText("");
                    newFF.setError(ex.getMessage());
                    Toast.error(getContentPane(), ex.getMessage());
                });
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, Spacing.S));
        buttons.setOpaque(false);
        buttons.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.L, Spacing.XL));
        buttons.add(cancel);
        buttons.add(save);
        getRootPane().setDefaultButton(save);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private static boolean constantEquals(char[] a, char[] b) {
        if (a == null || b == null) return false;
        byte[] ab = toBytes(a);
        byte[] bb = toBytes(b);
        try { return MessageDigest.isEqual(ab, bb); }
        finally {
            Arrays.fill(ab, (byte) 0);
            Arrays.fill(bb, (byte) 0);
        }
    }

    private static byte[] toBytes(char[] chars) {
        byte[] out = new byte[chars.length * 2];
        for (int i = 0; i < chars.length; i++) {
            out[i * 2]     = (byte) ((chars[i] >> 8) & 0xFF);
            out[i * 2 + 1] = (byte) (chars[i] & 0xFF);
        }
        return out;
    }
}
