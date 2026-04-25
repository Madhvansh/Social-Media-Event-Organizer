package com.eventorganizer.ui.laf;

import com.eventorganizer.ui.fx.Animator;
import com.eventorganizer.ui.theme.Easing;
import com.eventorganizer.ui.theme.Motion;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * Text field with an animated bottom-accent underline that grows from the
 * middle when focus arrives and retracts when focus leaves. Supports optional
 * placeholder text shown when the field is empty.
 *
 * <p>Base rendering is still delegated to FlatLaf; the underline overlay is
 * painted in {@link #paintComponent(Graphics)} after super.
 */
public final class AuroraTextField extends JTextField {

    private float underline = 0f;
    private String placeholder;
    private Animator.Handle handle;

    public AuroraTextField() { this(""); }
    public AuroraTextField(String placeholder) {
        super(24);
        this.placeholder = placeholder;
        putClientProperty("JTextField.placeholderText", placeholder);
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { animate(1f); }
            @Override public void focusLost(FocusEvent e)   { animate(0f); }
        });
        getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { repaint(); }
            @Override public void removeUpdate(DocumentEvent e) { repaint(); }
            @Override public void changedUpdate(DocumentEvent e) { repaint(); }
        });
    }

    public void setPlaceholder(String text) {
        this.placeholder = text;
        putClientProperty("JTextField.placeholderText", text);
        repaint();
    }

    public String getPlaceholder() { return placeholder; }

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
            Color c = Theme.ACCENT;
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine((int) (cx - half), y, (int) (cx + half), y);
        } finally {
            g2.dispose();
        }
    }
}
