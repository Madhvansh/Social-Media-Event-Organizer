package com.eventorganizer.ui.laf;

import com.eventorganizer.ui.fx.Animator;
import com.eventorganizer.ui.theme.Easing;
import com.eventorganizer.ui.theme.Motion;
import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import java.awt.geom.RoundRectangle2D;

/**
 * JButton with Obsidian Aurora signature. We paint the background ourselves
 * (instead of relying on FlatLaf's {@code JButton.buttonType=default} hint,
 * which only kicks in for the root pane's actual default button) so every
 * variant renders with a guaranteed-readable contrast pairing.
 *
 * <p>Variants:
 * <ul>
 *   <li><b>DEFAULT</b> — bronze fill, obsidian text. Primary CTAs.</li>
 *   <li><b>OUTLINE</b> — elevated bg + border, primary-text. Secondary actions.</li>
 *   <li><b>GHOST</b> — transparent bg until hover. Tertiary actions, footer/util.</li>
 *   <li><b>DANGER</b> — danger-red outline + text. Destructive actions.</li>
 * </ul>
 *
 * <p>All variants get press ripple + 2-layer focus halo. Hover lerp into the
 * target colour over {@link Motion#MICRO} ms.
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
        // We paint the background ourselves so the LAF can't override it.
        setContentAreaFilled(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        setForegroundForVariant();
        attachMouseListener();
    }

    public void setVariant(Variant v) {
        this.variant = v;
        setForegroundForVariant();
        repaint();
    }

    private void setForegroundForVariant() {
        switch (variant) {
            case DEFAULT: setForeground(new Color(0x1B1612)); break;
            case DANGER:  setForeground(Theme.DANGER);        break;
            case OUTLINE: setForeground(Theme.TEXT_PRIMARY);  break;
            case GHOST:
            default:      setForeground(Theme.TEXT_SECONDARY);
        }
    }

    private void attachMouseListener() {
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (!isEnabled()) return;
                startRipple(e.getPoint());
            }
            @Override public void mouseEntered(MouseEvent e) { repaint(); }
            @Override public void mouseExited(MouseEvent e)  { repaint(); }
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
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int arc = Radius.LG;

            paintBackground(g2, w, h, arc);

            // Ripple — clipped to the rounded rect so it doesn't bleed outside.
            if (ripplePoint != null && rippleAlpha > 0f) {
                g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
                Color c = (variant == Variant.DEFAULT) ? new Color(27, 22, 18) : Theme.ACCENT;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rippleAlpha));
                g2.setColor(c);
                g2.fillOval(
                    (int) (ripplePoint.x - rippleRadius),
                    (int) (ripplePoint.y - rippleRadius),
                    (int) (rippleRadius * 2),
                    (int) (rippleRadius * 2));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                g2.setClip(null);
            }
        } finally {
            g2.dispose();
        }

        // Let super render the text/icon on top of our background.
        super.paintComponent(g);
    }

    private void paintBackground(Graphics2D g, int w, int h, int arc) {
        boolean disabled = !isEnabled();
        boolean pressed = getModel().isPressed();
        boolean hover = getModel().isRollover();

        switch (variant) {
            case DEFAULT: {
                Color fill = disabled ? Theme.BG_OVERLAY
                    : pressed ? Theme.ACCENT_PRESSED
                    : hover ? Theme.ACCENT_HOVER
                    : Theme.ACCENT;
                g.setColor(fill);
                g.fillRoundRect(0, 0, w, h, arc, arc);
                break;
            }
            case DANGER: {
                Color stroke = disabled ? Theme.BORDER : Theme.DANGER;
                g.setColor(disabled ? Theme.BG_ELEVATED
                    : hover ? Theme.DANGER_SOFT : Theme.BG_ELEVATED);
                g.fillRoundRect(0, 0, w, h, arc, arc);
                g.setColor(stroke);
                g.setStroke(new BasicStroke(1.2f));
                g.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                break;
            }
            case OUTLINE: {
                Color fill = disabled ? Theme.BG_ELEVATED
                    : pressed ? new Color(0x1F1A15)
                    : hover ? Theme.BG_OVERLAY : Theme.BG_ELEVATED;
                g.setColor(fill);
                g.fillRoundRect(0, 0, w, h, arc, arc);
                g.setColor(Theme.BORDER);
                g.setStroke(new BasicStroke(1f));
                g.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                break;
            }
            case GHOST:
            default: {
                if (hover && !disabled) {
                    g.setColor(Theme.BG_OVERLAY);
                    g.fillRoundRect(0, 0, w, h, arc, arc);
                }
                break;
            }
        }
    }

    @Override
    protected void paintBorder(Graphics g) {
        if (!isFocusOwner() || Motion.REDUCED) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = Radius.LG;
            int[] widths = { 7, 4, 2 };
            float[] mults = { 0.18f, 0.32f, 0.55f };
            Color base = (variant == Variant.DANGER) ? Theme.DANGER : Theme.ACCENT;
            for (int i = 0; i < widths.length; i++) {
                int a = Math.round(255 * mults[i]);
                g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), a));
                g2.setStroke(new BasicStroke(widths[i],
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, arc, arc);
            }
        } finally {
            g2.dispose();
        }
    }

    @Override
    public Insets getInsets() {
        Insets base = super.getInsets();
        return new Insets(base.top, base.left, base.bottom, base.right);
    }
}
