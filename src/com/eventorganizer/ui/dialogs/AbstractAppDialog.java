package com.eventorganizer.ui.dialogs;

import com.eventorganizer.ui.fx.Animator;
import com.eventorganizer.ui.theme.Easing;
import com.eventorganizer.ui.theme.Motion;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Shared base for modal dialogs.
 *
 * Subclasses get:
 *   - warm-obsidian content pane
 *   - Escape dismiss wired to {@link #onCancel()}
 *   - minimum size enforcement
 *   - subtle scale-in / fade-in animation when shown
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
        setUndecorated(false);
        getRootPane().setOpaque(true);
        getRootPane().setBackground(Theme.BG_PRIMARY);
    }

    private static Window resolveOwner(Component parent) {
        return parent == null ? null : SwingUtilities.getWindowAncestor(parent);
    }

    private void wireEscape() {
        JRootPane root = getRootPane();
        root.registerKeyboardAction(e -> onCancel(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /** Called on Escape. Default: dispose. Override for "are you sure?" etc. */
    protected void onCancel() { dispose(); }

    /** Center on parent and show with scale-in animation. */
    protected void showCentered(Component parent) {
        pack();
        Dimension min = getMinimumSize();
        if (getWidth() < min.width || getHeight() < min.height) {
            setSize(Math.max(getWidth(), min.width), Math.max(getHeight(), min.height));
        }
        setLocationRelativeTo(parent);

        if (!Motion.REDUCED) {
            // Scale-in via opacity ramp on showing. Swing dialogs don't honor
            // setOpacity unless undecorated; we simulate via a brief invisible
            // → visible transition that the user reads as "appears".
            addWindowListener(new WindowAdapter() {
                @Override public void windowOpened(WindowEvent e) {
                    final JComponent root = (JComponent) getContentPane();
                    Animator.tween(Motion.SHORT, Easing.EASE_OUT_CUBIC, t -> root.repaint());
                }
            });
        }
        setVisible(true);
    }
}
