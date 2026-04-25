package com.eventorganizer.ui.components;

import com.eventorganizer.ui.fx.Animator;
import com.eventorganizer.ui.theme.Easing;
import com.eventorganizer.ui.theme.Elevation;
import com.eventorganizer.ui.theme.Iconography;
import com.eventorganizer.ui.theme.Motion;
import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

/**
 * Bottom-center toast stack. Each toast slides up 12 px and fades in on arrival,
 * then fades + slides down 6 px on dismissal. Up to {@value #MAX_STACK} toasts
 * are visible at once; additional notifications dismiss the oldest.
 *
 * <p>API preserved from the original Toast: {@link #info}/{@link #success}/
 * {@link #warning}/{@link #error} taking a component anchor + message.
 */
public final class Toast {
    private Toast() {}

    private static final int MAX_STACK = 3;
    private static final int TOAST_W = 360;
    private static final int TOAST_H = 56;
    private static final int GAP = 8;
    private static final int BOTTOM_PAD = Spacing.XXL;

    private static final List<ToastView> ACTIVE = new ArrayList<>();

    public static void info(Component anchor, String msg)    { show(anchor, msg, Kind.INFO);    }
    public static void success(Component anchor, String msg) { show(anchor, msg, Kind.SUCCESS); }
    public static void warning(Component anchor, String msg) { show(anchor, msg, Kind.WARNING); }
    public static void error(Component anchor, String msg)   { show(anchor, msg, Kind.ERROR);   }

    private enum Kind {
        INFO    (Theme.INFO,    "envelope"),
        SUCCESS (Theme.SUCCESS, "check"),
        WARNING (Theme.WARNING, "star"),
        ERROR   (Theme.DANGER,  "x");

        final Color color;
        final String icon;
        Kind(Color color, String icon) { this.color = color; this.icon = icon; }
    }

    private static void show(Component anchor, String msg, Kind kind) {
        Window w = anchor == null ? null : SwingUtilities.getWindowAncestor(anchor);
        if (!(w instanceof JFrame)) return;
        JFrame frame = (JFrame) w;
        JLayeredPane layers = frame.getLayeredPane();

        ToastView toast = new ToastView(msg, kind);

        // Evict oldest if over MAX_STACK
        while (ACTIVE.size() >= MAX_STACK) {
            ToastView old = ACTIVE.remove(0);
            old.dismissNow(layers);
        }
        ACTIVE.add(toast);

        restack(frame);

        layers.add(toast, JLayeredPane.POPUP_LAYER);
        layers.moveToFront(toast);
        toast.animateIn(layers, frame);

        Timer hold = new Timer(Motion.TOAST_HOLD_MS, ev -> {
            ACTIVE.remove(toast);
            toast.animateOut(layers);
            restack(frame);
        });
        hold.setRepeats(false);
        hold.start();
    }

    private static void restack(JFrame frame) {
        int baseY = frame.getHeight() - TOAST_H - BOTTOM_PAD;
        int x = (frame.getWidth() - TOAST_W) / 2;
        for (int i = ACTIVE.size() - 1, slot = 0; i >= 0; i--, slot++) {
            ToastView t = ACTIVE.get(i);
            int y = baseY - slot * (TOAST_H + GAP);
            t.setBounds(x, y, TOAST_W, TOAST_H);
        }
    }

    private static final class ToastView extends JComponent {
        private final String message;
        private final Kind kind;
        private float alpha = 0f;
        private float slide = 12f;

        ToastView(String message, Kind kind) {
            this.message = message == null ? "" : message;
            this.kind = kind;
            setOpaque(false);
            setPreferredSize(new Dimension(TOAST_W, TOAST_H));
        }

        void animateIn(JLayeredPane layers, JFrame frame) {
            if (Motion.REDUCED) { alpha = 1f; slide = 0f; repaint(); return; }
            Animator.tween(Motion.TOAST_FADE_IN_MS, Easing.EASE_OUT_CUBIC, t -> {
                alpha = (float) t;
                slide = 12f * (1f - (float) t);
                repaint();
            });
        }

        void animateOut(JLayeredPane layers) {
            if (Motion.REDUCED) { dismissNow(layers); return; }
            final float startAlpha = alpha;
            Animator.tween(Motion.TOAST_FADE_OUT_MS, Easing.EASE_OUT_CUBIC, t -> {
                alpha = startAlpha * (1f - (float) t);
                slide = -6f * (float) t;
                repaint();
            }, () -> dismissNow(layers));
        }

        void dismissNow(JLayeredPane layers) {
            layers.remove(this);
            layers.repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

                int w = getWidth(), h = getHeight();
                int yOff = Math.round(slide);
                g2.translate(0, yOff);

                g2.setComposite(java.awt.AlphaComposite.getInstance(
                    java.awt.AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alpha))));

                Elevation.paint(g2, 0, 0, w, h - 2, Radius.LG, Elevation.Tier.E2);

                g2.setColor(Theme.BG_ELEVATED);
                g2.fillRoundRect(0, 0, w, h - 2, Radius.LG, Radius.LG);

                g2.setColor(kind.color);
                g2.fillRoundRect(0, 0, 3, h - 2, 3, 3);

                g2.setColor(Theme.BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, w - 1, h - 3, Radius.LG, Radius.LG);

                Iconography.paint(g2, kind.icon, 16, h / 2f - 9, 18f, kind.color);

                g2.setFont(Typography.BODY);
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Theme.TEXT_PRIMARY);
                int tx = 44;
                int ty = (h - 2 + fm.getAscent() - fm.getDescent()) / 2;
                String line = message;
                int maxW = w - tx - 16;
                if (fm.stringWidth(line) > maxW) {
                    int end = line.length();
                    while (end > 0 && fm.stringWidth(line.substring(0, end) + "…") > maxW) end--;
                    line = line.substring(0, Math.max(0, end)) + "…";
                }
                g2.drawString(line, tx, ty);
            } finally {
                g2.dispose();
            }
        }
    }
}
