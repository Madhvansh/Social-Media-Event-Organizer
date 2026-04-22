package com.eventorganizer.ui.dialogs;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.ui.components.FormField;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.utils.PasswordHasher;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.security.MessageDigest;
import java.util.Arrays;

public class ChangePasswordDialog {

    public static void show(Component parent, UIController controller) {
        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(owner, "Change Password", JDialog.ModalityType.APPLICATION_MODAL);
        d.setLayout(new BorderLayout());

        JPasswordField oldPw = new JPasswordField();
        JPasswordField newPw = new JPasswordField();
        JPasswordField confirmPw = new JPasswordField();

        FormField oldFF = new FormField("Current password", oldPw);
        FormField newFF = new FormField("New password", newPw);
        FormField confFF = new FormField("Confirm new password", confirmPw);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Theme.BG_PRIMARY);
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
        form.add(oldFF); form.add(newFF); form.add(confFF);

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> d.dispose());
        JButton save = new JButton("Change");
        save.setMnemonic('H');
        save.addActionListener(e -> {
            char[] a = oldPw.getPassword();
            char[] b = newPw.getPassword();
            char[] c = confirmPw.getPassword();
            try {
                if (!constantEquals(b, c)) {
                    confFF.setError("Passwords do not match.");
                    Toast.error(d.getContentPane(), "Passwords do not match.");
                    return;
                }
                controller.changePassword(a, b);
                Toast.success(parent, "Password changed.");
                d.dispose();
            } catch (AppException ex) {
                newFF.setError(ex.getMessage());
                Toast.error(d.getContentPane(), ex.getMessage());
            } finally {
                PasswordHasher.zero(a);
                PasswordHasher.zero(b);
                PasswordHasher.zero(c);
                oldPw.setText(""); newPw.setText(""); confirmPw.setText("");
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.add(cancel); buttons.add(save);
        d.getRootPane().setDefaultButton(save);

        d.add(form, BorderLayout.CENTER);
        d.add(buttons, BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
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
