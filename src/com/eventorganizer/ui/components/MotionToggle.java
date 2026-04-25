package com.eventorganizer.ui.components;

import com.eventorganizer.ui.theme.Motion;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;
import com.eventorganizer.utils.Preferences;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Self-contained pill-style toggle for {@link Motion#REDUCED}. Click to flip;
 * the value persists immediately via {@link Preferences#saveMotionReduced()}.
 *
 * <p>Layout: tracked-uppercase label "REDUCED MOTION" on the left, a 36×20
 * track with a sliding 16 px knob on the right. When ON, the track fills
 * with bronze; when OFF, the knob sits left on a neutral track.
 *
 * <p>Pure paintComponent-based — no Swing JCheckBox, so it inherits the
 * Aurora palette without any LAF override fight.
 */
public final class MotionToggle extends JComponent {

    private static final int TRACK_W = 36;
    private static final int TRACK_H = 20;
    private static final int GAP = 12;

    private boolean hover;

    public MotionToggle() {
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Toggle reduced motion (freezes the aurora backdrop and disables transitions).");
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                Motion.setReduced(!Motion.REDUCED);
                Preferences.saveMotionReduced();
                repaint();
            }
        });
        // Repaint when motion changes externally (e.g. another toggle elsewhere).
        Motion.addListener(this::repaint);
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(Typography.LABEL);
        int textW = fm.stringWidth("REDUCED MOTION") + 24;
        return new Dimension(textW + GAP + TRACK_W + 4, Math.max(TRACK_H + 8, fm.getHeight() + 8));
    }

    @Override public Dimension getMinimumSize() { return getPreferredSize(); }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

            int w = getWidth(), h = getHeight();
            FontMetrics fm = g2.getFontMetrics(Typography.LABEL);
            int textY = (h + fm.getAscent() - fm.getDescent()) / 2;

            Color labelColor = hover ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY;
            Typography.drawTracked(g2, "REDUCED MOTION", Typography.LABEL, 0.10f, 2, textY);

            // unused: avoid color warning
            g2.setColor(labelColor);

            // Track on the right.
            int trackX = w - TRACK_W - 2;
            int trackY = (h - TRACK_H) / 2;

            boolean on = Motion.REDUCED;
            Color trackBg = on ? Theme.ACCENT : Theme.BG_OVERLAY;
            Color trackBorder = on ? Theme.ACCENT_HOVER : Theme.BORDER;
            g2.setColor(trackBg);
            g2.fillRoundRect(trackX, trackY, TRACK_W, TRACK_H, TRACK_H, TRACK_H);
            g2.setColor(trackBorder);
            g2.drawRoundRect(trackX, trackY, TRACK_W - 1, TRACK_H - 1, TRACK_H, TRACK_H);

            // Knob.
            int knobD = TRACK_H - 6;
            int knobX = on ? trackX + TRACK_W - knobD - 3 : trackX + 3;
            int knobY = trackY + 3;
            g2.setColor(on ? new Color(0x1B1612) : Theme.TEXT_SECONDARY);
            g2.fillOval(knobX, knobY, knobD, knobD);
        } finally {
            g2.dispose();
        }
    }
}
