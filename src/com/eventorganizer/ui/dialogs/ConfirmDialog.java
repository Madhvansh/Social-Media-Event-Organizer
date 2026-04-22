package com.eventorganizer.ui.dialogs;

import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;

public final class ConfirmDialog {
    private ConfirmDialog() {}

    public static boolean ask(Component parent, String title, String message, String confirmLabel) {
        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(owner, title, JDialog.ModalityType.APPLICATION_MODAL);
        d.setLayout(new BorderLayout(0, 10));

        JLabel msg = new JLabel("<html><body style='width:320px'>" + message + "</body></html>");
        msg.setFont(Theme.FONT_BODY);
        msg.setForeground(Theme.TEXT_PRIMARY);
        msg.setBorder(BorderFactory.createEmptyBorder(18, 20, 12, 20));

        boolean[] result = {false};

        JButton cancel = new JButton("Cancel");
        cancel.setMnemonic('C');
        cancel.addActionListener(e -> d.dispose());

        JButton ok = new JButton(confirmLabel == null ? "Confirm" : confirmLabel);
        ok.setForeground(Theme.DANGER);
        ok.addActionListener(e -> { result[0] = true; d.dispose(); });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.add(cancel);
        buttons.add(ok);

        d.add(msg, BorderLayout.CENTER);
        d.add(buttons, BorderLayout.SOUTH);

        JRootPane root = d.getRootPane();
        root.setDefaultButton(cancel);
        root.registerKeyboardAction(e -> d.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent_CONDITION_IN_FOCUSED_WINDOW());

        d.pack();
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
        return result[0];
    }

    private static int JComponent_CONDITION_IN_FOCUSED_WINDOW() {
        return javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
    }
}
