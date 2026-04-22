package com.eventorganizer.ui.dialogs;

import com.eventorganizer.ui.theme.Theme;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.KeyEvent;

/**
 * Shared base for modal dialogs.
 *
 * Subclasses get:
 *   - warm-obsidian content pane
 *   - Escape dismiss wired to {@link #onCancel()}
 *   - minimum size enforcement
 *
 * Subclasses build their own content via {@link #getContentPane()} and override
 * {@link #onCancel()} to decide what Escape / X button means for them.
 */
public abstract class AbstractAppDialog extends JDialog {

    protected AbstractAppDialog(Component parent, String title, int minWidth, int minHeight) {
        super(resolveOwner(parent), title, ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_PRIMARY);
        setMinimumSize(new Dimension(minWidth, minHeight));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        wireEscape();
    }

    private static Window resolveOwner(Component parent) {
        return parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
    }

    private void wireEscape() {
        JRootPane root = getRootPane();
        root.registerKeyboardAction(e -> onCancel(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /** Called on Escape. Default: dispose. Override for "are you sure?" etc. */
    protected void onCancel() { dispose(); }

    /** Center on parent and show. Subclasses call this after building content. */
    protected void showCentered(Component parent) {
        pack();
        Dimension min = getMinimumSize();
        if (getWidth() < min.width || getHeight() < min.height) {
            setSize(Math.max(getWidth(), min.width), Math.max(getHeight(), min.height));
        }
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}
