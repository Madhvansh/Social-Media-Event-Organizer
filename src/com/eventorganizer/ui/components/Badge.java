package com.eventorganizer.ui.components;

import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

/**
 * Small pill label. Reads background + foreground from one of five palette kinds.
 * Rounded via paintComponent, not setBorder, so the rounding is pixel-clean at
 * the 11px font size.
 */
public class Badge extends JLabel {

    public enum Kind { DEFAULT, ACCENT, SUCCESS, WARNING, DANGER, INFO }

    private Color fill;
    private Color strokeColor;

    public Badge(String text) { this(text, Kind.DEFAULT); }

    /** Legacy: maps a bare palette color to the nearest Kind. New code should pass Kind directly. */
    public Badge(String text, Color color) {
        this(text, kindFromColor(color));
    }

    private static Kind kindFromColor(Color c) {
        if (c == null) return Kind.DEFAULT;
        if (c.equals(Theme.ACCENT)  || c.equals(Theme.ACCENT_HOVER))  return Kind.ACCENT;
        if (c.equals(Theme.SUCCESS)) return Kind.SUCCESS;
        if (c.equals(Theme.WARNING)) return Kind.WARNING;
        if (c.equals(Theme.DANGER))  return Kind.DANGER;
        if (c.equals(Theme.INFO))    return Kind.INFO;
        return Kind.DEFAULT;
    }

    public Badge(String text, Kind kind) {
        super(text);
        applyKind(kind);
        setForegroundByKind(kind);
        setFont(Theme.FONT_SMALL);
        setOpaque(false);
        Insets pad = new Insets(Spacing.XS, Spacing.S, Spacing.XS, Spacing.S);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(pad.top, pad.left, pad.bottom, pad.right));
    }

    public void setKind(Kind kind) {
        applyKind(kind);
        setForegroundByKind(kind);
        repaint();
    }

    private void applyKind(Kind kind) {
        switch (kind) {
            case ACCENT:  fill = Theme.ACCENT_SOFT;  strokeColor = Theme.ACCENT;  break;
            case SUCCESS: fill = Theme.SUCCESS_SOFT; strokeColor = Theme.SUCCESS; break;
            case WARNING: fill = Theme.WARNING_SOFT; strokeColor = Theme.WARNING; break;
            case DANGER:  fill = Theme.DANGER_SOFT;  strokeColor = Theme.DANGER;  break;
            case INFO:    fill = Theme.INFO_SOFT;    strokeColor = Theme.INFO;    break;
            case DEFAULT:
            default:      fill = Theme.BG_HOVER;     strokeColor = Theme.BORDER;  break;
        }
    }

    private void setForegroundByKind(Kind kind) {
        switch (kind) {
            case ACCENT:  setForeground(Theme.ACCENT);  break;
            case SUCCESS: setForeground(Theme.SUCCESS); break;
            case WARNING: setForeground(Theme.WARNING); break;
            case DANGER:  setForeground(Theme.DANGER);  break;
            case INFO:    setForeground(Theme.INFO);    break;
            case DEFAULT:
            default:      setForeground(Theme.TEXT_MUTED); break;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), Radius.SM * 2, Radius.SM * 2);
            if (strokeColor != null) {
                g2.setColor(strokeColor);
                g2.setStroke(new java.awt.BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Radius.SM * 2, Radius.SM * 2);
            }
        } finally {
            g2.dispose();
        }
        super.paintComponent(g);
    }
}
