package com.eventorganizer.ui.laf;

import com.eventorganizer.ui.fx.Animator;
import com.eventorganizer.ui.theme.Easing;
import com.eventorganizer.ui.theme.Motion;
import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * JButton with Obsidian Aurora signature: press-ripple (radial expansion from
 * the click origin, 340 ms) and a two-layer focus halo (2 px border glow plus
 * an 8 px outer accent halo). Base rendering is left to FlatLaf — this class
 * only adds overlays.
 *
 * <p>Three visual variants:
 * <ul>
 *   <li><b>DEFAULT</b> — solid accent fill, obsidian text. {@code JButton.buttonType=default}.</li>
 *   <li><b>OUTLINE</b> — ghost fill with bordered outline (the FlatLaf default look).</li>
 *   <li><b>GHOST</b> — no border, transparent bg until hover; for tertiary actions.</li>
 * </ul>
 */
public final class AuroraButton extends JButton {

    public enum Variant { DEFAULT, OUTLINE, GHOST, DANGER }

    private Variant variant = Variant.OUTLINE;
    private Point  ripplePoint;
    private float  rippleRadius;
    private float  rippleAlpha;
    private Animator.Handle rippleHandle;

    public AuroraButton() { this("", Variant.OUTLINE); }
    public AuroraButton(String text) { this(text, Variant.OUTLINE); }

    public AuroraButton(String text, Variant variant) {
        super(text);
        setVariant(variant);
        setFocusPainted(false);
        setContentAreaFilled(true);
        setBorder(outerHaloBorder());
        attachMouseListener();
    }

    public void setVariant(Variant v) {
        this.variant = v;
        switch (v) {
            case DEFAULT:
                putClientProperty("JButton.buttonType", "default");
                setForeground(new Color(0x1B1612));
                break;
            case GHOST:
                putClientProperty("JButton.buttonType", "borderless");
                setForeground(Theme.TEXT_PRIMARY);
                break;
            case DANGER:
                putClientProperty("JButton.buttonType", null);
                setForeground(Theme.DANGER);
                break;
            case OUTLINE:
            default:
                putClientProperty("JButton.buttonType", null);
                setForeground(Theme.TEXT_PRIMARY);
        }
        repaint();
    }

    private void attachMouseListener() {
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (!isEnabled()) return;
                startRipple(e.getPoint());
            }
        };
        addMouseListener(ma);
    }

    private void startRipple(Point origin) {
        if (Motion.REDUCED) return;
        if (rippleHandle != null) rippleHandle.cancel();
        ripplePoint = origin;
        rippleRadius = 0f;
        rippleAlpha = 0.32f;
        float maxR = (float) Math.hypot(getWidth(), getHeight());
        rippleHandle = Animator.tween(Motion.LONG, Easing.EASE_OUT_QUINT, t -> {
            rippleRadius = (float) (maxR * t);
            rippleAlpha = 0.32f * (float) (1.0 - t);
            repaint();
        }, () -> { ripplePoint = null; repaint(); });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (ripplePoint == null || rippleAlpha <= 0f) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = Radius.LG;
            g2.setClip(new java.awt.geom.RoundRectangle2D.Float(
                0, 0, getWidth(), getHeight(), arc, arc));
            Color c = (variant == Variant.DEFAULT) ? new Color(27, 22, 18, 255) : Theme.ACCENT;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rippleAlpha));
            g2.setColor(c);
            g2.fillOval(
                (int) (ripplePoint.x - rippleRadius),
                (int) (ripplePoint.y - rippleRadius),
                (int) (rippleRadius * 2),
                (int) (rippleRadius * 2));
        } finally {
            g2.dispose();
        }
    }

    @Override
    protected void paintBorder(Graphics g) {
        super.paintBorder(g);
        if (!isFocusOwner() || Motion.REDUCED) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = Radius.LG;
            int[] widths = { 7, 4, 2 };
            float[] mults = { 0.28f, 0.55f, 0.90f };
            Color base = (variant == Variant.DANGER) ? Theme.DANGER : Theme.ACCENT;
            for (int i = 0; i < widths.length; i++) {
                int a = Math.round(255 * mults[i]);
                g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(),
                    Math.min(255, (int) (a * 0.35f))));
                g2.setStroke(new BasicStroke(widths[i],
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, arc, arc);
            }
        } finally {
            g2.dispose();
        }
    }

    private static Border outerHaloBorder() {
        return BorderFactory.createEmptyBorder(6, 14, 6, 14);
    }

    @Override
    public Insets getInsets() {
        Insets base = super.getInsets();
        return new Insets(base.top, base.left, base.bottom, base.right);
    }
}
