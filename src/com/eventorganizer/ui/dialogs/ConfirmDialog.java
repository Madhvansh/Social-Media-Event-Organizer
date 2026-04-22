package com.eventorganizer.ui.dialogs;

import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

public final class ConfirmDialog {
    private ConfirmDialog() {}

    public static boolean ask(Component parent, String title, String message, String confirmLabel) {
        Impl d = new Impl(parent, title, message, confirmLabel);
        d.showCentered(parent);
        return d.confirmed;
    }

    private static final class Impl extends AbstractAppDialog {
        boolean confirmed = false;

        Impl(Component parent, String title, String message, String confirmLabel) {
            super(parent, title, 400, 180);

            JLabel heading = new JLabel(title);
            heading.setFont(Theme.FONT_TITLE);
            heading.setForeground(Theme.TEXT_PRIMARY);
            heading.setBorder(BorderFactory.createEmptyBorder(Spacing.L, Spacing.L, Spacing.S, Spacing.L));

            JTextArea body = new JTextArea(message == null ? "" : message);
            body.setEditable(false);
            body.setFocusable(false);
            body.setLineWrap(true);
            body.setWrapStyleWord(true);
            body.setOpaque(false);
            body.setFont(Theme.FONT_BODY);
            body.setForeground(Theme.TEXT_PRIMARY);
            body.setBackground(Theme.BG_PRIMARY);
            body.setBorder(BorderFactory.createEmptyBorder(0, Spacing.L, Spacing.L, Spacing.L));

            JPanel content = new JPanel(new BorderLayout());
            content.setOpaque(false);
            content.add(heading, BorderLayout.NORTH);
            content.add(body, BorderLayout.CENTER);

            JButton cancel = new JButton("Cancel");
            cancel.setMnemonic('C');
            cancel.addActionListener(e -> dispose());

            JButton ok = new JButton(confirmLabel == null ? "Confirm" : confirmLabel);
            ok.setForeground(Theme.DANGER);
            ok.addActionListener(e -> { confirmed = true; dispose(); });

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, Spacing.S));
            buttons.setOpaque(false);
            buttons.setBorder(BorderFactory.createEmptyBorder(0, Spacing.L, Spacing.L, Spacing.L));
            buttons.add(cancel);
            buttons.add(ok);

            add(content, BorderLayout.CENTER);
            add(buttons, BorderLayout.SOUTH);

            JRootPane root = getRootPane();
            root.setDefaultButton(cancel);
        }
    }
}
