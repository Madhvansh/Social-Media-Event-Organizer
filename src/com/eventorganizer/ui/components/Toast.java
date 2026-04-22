package com.eventorganizer.ui.components;

import com.eventorganizer.ui.theme.Motion;
import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.SoftBorder;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;

/**
 * Glass-pane toast. Three variants: success / warning / error / info —
 * each tinted by a colored border on elevated surface. Auto-dismisses after
 * {@link Motion#TOAST_HOLD_MS}.
 */
public final class Toast {
    private Toast() {}

    public static void info(Component anchor, String msg)    { show(anchor, msg, Theme.INFO);    }
    public static void success(Component anchor, String msg) { show(anchor, msg, Theme.SUCCESS); }
    public static void warning(Component anchor, String msg) { show(anchor, msg, Theme.WARNING); }
    public static void error(Component anchor, String msg)   { show(anchor, msg, Theme.DANGER);  }

    private static void show(Component anchor, String msg, Color accent) {
        Window w = anchor == null ? null : SwingUtilities.getWindowAncestor(anchor);
        if (!(w instanceof JFrame)) return;
        JFrame frame = (JFrame) w;
        JLayeredPane layers = frame.getLayeredPane();

        JLabel toast = new JLabel(msg, SwingConstants.CENTER);
        toast.setOpaque(true);
        toast.setBackground(Theme.BG_ELEVATED);
        toast.setForeground(Theme.TEXT_PRIMARY);
        toast.setFont(Theme.FONT_BODY);
        toast.setBorder(BorderFactory.createCompoundBorder(
            SoftBorder.of(Radius.MD, accent, 1,
                new Insets(Spacing.M, Spacing.L, Spacing.M, Spacing.L)),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        Dimension pref = toast.getPreferredSize();
        int w0 = Math.max(240, Math.min(520, pref.width + Spacing.XXL));
        int h0 = Math.max(40, pref.height + Spacing.M);
        int x = (frame.getWidth() - w0) / 2;
        int y = frame.getHeight() - h0 - Spacing.XXL;
        toast.setBounds(x, y, w0, h0);

        layers.add(toast, JLayeredPane.POPUP_LAYER);
        layers.moveToFront(toast);

        Timer t = new Timer(Motion.TOAST_HOLD_MS, ev -> {
            layers.remove(toast);
            layers.repaint();
        });
        t.setRepeats(false);
        t.start();
    }
}
