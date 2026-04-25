package com.eventorganizer.ui.dialogs;

import com.eventorganizer.ui.laf.AuroraButton;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

/**
 * Single-action confirmation. Uses AuroraButton variants — Danger outline for
 * the destructive option, GHOST for cancel — with a typographic title in H1.
 */
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
            super(parent, title, 440, 200);

            JLabel heading = new JLabel(title);
            heading.setFont(Typography.H1);
            heading.setForeground(Theme.TEXT_PRIMARY);
            heading.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.S, Spacing.XL));

            JTextArea body = new JTextArea(message == null ? "" : message);
            body.setEditable(false);
            body.setFocusable(false);
            body.setLineWrap(true);
            body.setWrapStyleWord(true);
            body.setOpaque(false);
            body.setFont(Typography.BODY);
            body.setForeground(Theme.TEXT_SECONDARY);
            body.setBackground(Theme.BG_PRIMARY);
            body.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.L, Spacing.XL));

            JPanel content = new JPanel(new BorderLayout());
            content.setOpaque(false);
            content.add(heading, BorderLayout.NORTH);
            content.add(body, BorderLayout.CENTER);

            AuroraButton cancel = new AuroraButton("Cancel", AuroraButton.Variant.GHOST);
            cancel.setMnemonic('C');
            cancel.addActionListener(e -> dispose());

            AuroraButton ok = new AuroraButton(
                confirmLabel == null ? "Confirm" : confirmLabel,
                AuroraButton.Variant.DANGER);
            ok.addActionListener(e -> { confirmed = true; dispose(); });

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, Spacing.S));
            buttons.setOpaque(false);
            buttons.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.L, Spacing.XL));
            buttons.add(cancel);
            buttons.add(ok);

            add(content, BorderLayout.CENTER);
            add(buttons, BorderLayout.SOUTH);

            JRootPane root = getRootPane();
            root.setDefaultButton(cancel);
        }
    }
}
