package com.eventorganizer.ui.dialogs;

import com.eventorganizer.ui.components.AsyncUI;
import com.eventorganizer.ui.components.FormField;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.laf.AuroraButton;
import com.eventorganizer.ui.laf.AuroraPasswordField;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;
import com.eventorganizer.utils.PasswordHasher;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * Change password dialog. AuroraPasswordField inputs and an inline strength
 * meter (zxcvbn-lite heuristic — no dependency, just classes/length scoring).
 */
public class ChangePasswordDialog extends AbstractAppDialog {

    public static void show(Component parent, UIController controller) {
        new ChangePasswordDialog(parent, controller).showCentered(parent);
    }

    private ChangePasswordDialog(Component parent, UIController controller) {
        super(parent, "Change password", 460, 460);

        AuroraPasswordField oldPw = new AuroraPasswordField("Current password");
        AuroraPasswordField newPw = new AuroraPasswordField("New password");
        AuroraPasswordField confirmPw = new AuroraPasswordField("Confirm new password");

        FormField oldFF  = new FormField("CURRENT PASSWORD", oldPw);
        FormField newFF  = new FormField("NEW PASSWORD", newPw);
        FormField confFF = new FormField("CONFIRM PASSWORD", confirmPw);

        StrengthMeter meter = new StrengthMeter();
        newPw.getDocument().addDocumentListener(new DocumentListener() {
            private void apply() {
                meter.setStrength(score(newPw.getPassword()));
            }
            @Override public void insertUpdate(DocumentEvent e) { apply(); }
            @Override public void removeUpdate(DocumentEvent e) { apply(); }
            @Override public void changedUpdate(DocumentEvent e) { apply(); }
        });

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.L, Spacing.XL));
        form.add(oldFF);
        form.add(newFF);
        form.add(meter);
        form.add(javax.swing.Box.createVerticalStrut(Spacing.S));
        form.add(confFF);

        AuroraButton cancel = new AuroraButton("Cancel", AuroraButton.Variant.GHOST);
        cancel.addActionListener(e -> dispose());
        AuroraButton save = new AuroraButton("Change password", AuroraButton.Variant.DEFAULT);
        save.setMnemonic('H');
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

    /**
     * Lightweight strength heuristic: 0–4 ladder based on length + character
     * class diversity. Not a substitute for zxcvbn, but enough to nudge users
     * away from "password" / "qwerty" without a dependency.
     */
    private static int score(char[] pw) {
        if (pw == null) return 0;
        int len = pw.length;
        if (len == 0) return 0;
        int classes = 0;
        boolean lower = false, upper = false, digit = false, sym = false;
        for (char c : pw) {
            if (Character.isLowerCase(c)) lower = true;
            else if (Character.isUpperCase(c)) upper = true;
            else if (Character.isDigit(c)) digit = true;
            else sym = true;
        }
        if (lower) classes++;
        if (upper) classes++;
        if (digit) classes++;
        if (sym)   classes++;

        if (len < 6) return 1;
        if (len < 9 && classes <= 2) return 2;
        if (len < 12 && classes <= 3) return 3;
        return 4;
    }

    /** Five-tick strength meter: dim base + filled segments by score. */
    private static final class StrengthMeter extends JComponent {
        private int strength = 0;

        StrengthMeter() {
            setOpaque(false);
            Dimension d = new Dimension(0, 22);
            setPreferredSize(new Dimension(280, 22));
            setMinimumSize(new Dimension(120, 22));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setBorder(BorderFactory.createEmptyBorder(0, 0, Spacing.S, 0));
        }

        void setStrength(int s) {
            this.strength = Math.max(0, Math.min(4, s));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = 6;
                int gap = 4;
                int barW = (w - gap * 4) / 5;

                Color fill;
                switch (strength) {
                    case 0: case 1: fill = Theme.DANGER; break;
                    case 2: fill = Theme.WARNING; break;
                    case 3: fill = Theme.ACCENT; break;
                    case 4:
                    default: fill = Theme.SUCCESS;
                }

                for (int i = 0; i < 5; i++) {
                    int x = i * (barW + gap);
                    g2.setColor(i < strength ? fill : Theme.BG_OVERLAY);
                    g2.fillRoundRect(x, 0, barW, h, h, h);
                }

                g2.setFont(Typography.SMALL);
                String label;
                switch (strength) {
                    case 0: label = ""; break;
                    case 1: label = "Weak"; break;
                    case 2: label = "Fair"; break;
                    case 3: label = "Strong"; break;
                    default: label = "Excellent";
                }
                if (!label.isEmpty()) {
                    g2.setColor(fill);
                    g2.drawString(label, 0, h + 14);
                }
            } finally {
                g2.dispose();
            }
        }
    }
}
