package com.eventorganizer.ui.laf;

import com.eventorganizer.ui.fx.Animator;
import com.eventorganizer.ui.theme.Easing;
import com.eventorganizer.ui.theme.Motion;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.JPasswordField;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/** Password field twin of {@link AuroraTextField} with the same focus underline. */
public final class AuroraPasswordField extends JPasswordField {

    private float underline = 0f;
    private Animator.Handle handle;

    public AuroraPasswordField() { this(""); }
    public AuroraPasswordField(String placeholder) {
        super(24);
        putClientProperty("JTextField.placeholderText", placeholder);
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { animate(1f); }
            @Override public void focusLost(FocusEvent e)   { animate(0f); }
        });
    }

    public void setPlaceholder(String text) {
        putClientProperty("JTextField.placeholderText", text);
        repaint();
    }

    private void animate(float target) {
        if (handle != null) handle.cancel();
        final float start = underline;
        if (Motion.REDUCED) { underline = target; repaint(); return; }
        handle = Animator.tween(Motion.SHORT, Easing.EASE_OUT_CUBIC, t -> {
            underline = start + (target - start) * (float) t;
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (underline <= 0.01f) return;
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int cx = getWidth() / 2;
            int y = getHeight() - 2;
            float half = (getWidth() * 0.46f) * underline;
            g2.setColor(Theme.ACCENT);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine((int) (cx - half), y, (int) (cx + half), y);
        } finally {
            g2.dispose();
        }
    }
}
