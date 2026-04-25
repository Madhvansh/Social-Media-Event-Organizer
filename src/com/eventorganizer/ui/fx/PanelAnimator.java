package com.eventorganizer.ui.fx;

import com.eventorganizer.ui.theme.Easing;
import com.eventorganizer.ui.theme.Motion;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.plaf.LayerUI;
import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * {@link LayerUI} that crossfades the wrapped component whenever
 * {@link #trigger()} is called externally — use it to animate panel switches
 * inside a {@link java.awt.CardLayout} without any flicker.
 *
 * <p>Intended wiring:
 * <pre>{@code
 *   PanelAnimator ui = new PanelAnimator();
 *   JLayer<JPanel> layer = new JLayer<>(centerPanel, ui);
 *   ...
 *   cardLayout.show(centerPanel, "reports");
 *   ui.trigger();  // fade the just-revealed card in
 * }</pre>
 */
public final class PanelAnimator extends LayerUI<JComponent> {

    private float alpha = 1f;
    private JLayer<?> current;

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        if (c instanceof JLayer) {
            current = (JLayer<?>) c;
            current.setLayerEventMask(0);
        }
    }

    @Override
    public void uninstallUI(JComponent c) {
        current = null;
        super.uninstallUI(c);
    }

    public void trigger() {
        if (Motion.REDUCED || current == null) {
            alpha = 1f;
            if (current != null) current.repaint();
            return;
        }
        alpha = 0f;
        final JLayer<?> layer = current;
        Animator.tween(Motion.MED, Easing.EASE_OUT_CUBIC, t -> {
            alpha = (float) t;
            layer.repaint();
        });
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        if (alpha >= 1f) {
            super.paint(g, c);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                Math.max(0f, Math.min(1f, alpha))));
            super.paint(g2, c);
        } finally {
            g2.dispose();
        }
    }

    /** Convenience factory that wraps a component in a JLayer with this UI installed. */
    public static JLayer<JComponent> wrap(Container panel) {
        return new JLayer<>((JComponent) panel, new PanelAnimator());
    }

    /** Helper: retrieves this UI from a JLayer wrapping it, or null. */
    @SuppressWarnings("unused")
    public static PanelAnimator of(JLayer<?> layer) {
        Object ui = layer.getUI();
        return (ui instanceof PanelAnimator) ? (PanelAnimator) ui : null;
    }

    @SuppressWarnings("unused")
    private Component currentRoot() { return current; }
}
