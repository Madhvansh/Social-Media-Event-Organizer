package com.eventorganizer.ui.components;

import com.eventorganizer.models.enums.RSVPStatus;
import com.eventorganizer.ui.fx.Animator;
import com.eventorganizer.ui.theme.Easing;
import com.eventorganizer.ui.theme.Motion;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.EnumMap;
import java.util.Map;

/**
 * Animated RSVP donut chart — the hero visual of the EventDetailsDialog.
 *
 * <p>Four segments (ACCEPTED / MAYBE / DECLINED / PENDING) sweep from 0° to
 * their target extents over {@link Motion#HERO} ms on first paint. Hovering a
 * segment grows it 6 px outward and highlights the legend pairing.
 * The hole displays the confirmed (accepted) count in {@link Typography#NUMERAL}.
 */
public final class RsvpDonut extends JComponent {

    private final Map<RSVPStatus, Long> counts = new EnumMap<>(RSVPStatus.class);
    private long total = 0;
    private float revealT = 0f;
    private int hoverSeg = -1;
    private Animator.Handle handle;

    public RsvpDonut() {
        setOpaque(false);
        for (RSVPStatus s : RSVPStatus.values()) counts.put(s, 0L);
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) { updateHover(e); }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) {
                if (hoverSeg != -1) { hoverSeg = -1; repaint(); }
            }
        });
    }

    public void setCounts(Map<RSVPStatus, Long> newCounts) {
        long t = 0;
        for (RSVPStatus s : RSVPStatus.values()) {
            long v = newCounts != null && newCounts.get(s) != null ? newCounts.get(s) : 0L;
            counts.put(s, v);
            t += v;
        }
        total = t;
        playReveal();
    }

    private void playReveal() {
        if (handle != null) handle.cancel();
        if (Motion.REDUCED) { revealT = 1f; repaint(); return; }
        revealT = 0f;
        handle = Animator.tween(Motion.HERO, Easing.EASE_OUT_QUINT, t -> {
            revealT = (float) t;
            repaint();
        });
    }

    private void updateHover(MouseEvent e) {
        int w = getWidth(), h = getHeight();
        int d = Math.min(w, h) - 12;
        if (d <= 0) return;
        int cx = w / 2, cy = h / 2;
        float ring = d / 2f;
        float rInner = ring * 0.62f;

        float dx = e.getX() - cx, dy = e.getY() - cy;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < rInner || dist > ring + 6) {
            if (hoverSeg != -1) { hoverSeg = -1; repaint(); }
            return;
        }
        double angle = Math.toDegrees(Math.atan2(-dy, dx));
        if (angle < 0) angle += 360;
        double sweep = -angle + 90;
        if (sweep < 0) sweep += 360;

        double offset = 0;
        int pick = -1;
        RSVPStatus[] order = { RSVPStatus.ACCEPTED, RSVPStatus.MAYBE,
                                RSVPStatus.DECLINED, RSVPStatus.PENDING };
        for (int i = 0; i < order.length; i++) {
            long c = counts.getOrDefault(order[i], 0L);
            if (total <= 0 || c <= 0) continue;
            double frac = 360.0 * c / total;
            if (sweep >= offset && sweep < offset + frac) { pick = i; break; }
            offset += frac;
        }
        if (pick != hoverSeg) {
            hoverSeg = pick;
            repaint();
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(180, 180);
    }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth(), h = getHeight();
        int d = Math.min(w, h) - 12;
        if (d <= 0) return;
        int cx = w / 2, cy = h / 2;
        float ring = d / 2f;
        float rInner = ring * 0.62f;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

            // Track ring
            g2.setColor(Theme.BG_ELEVATED);
            g2.setStroke(new BasicStroke(ring - rInner,
                BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            float mid = (ring + rInner) / 2f;
            g2.draw(new Ellipse2D.Float(cx - mid, cy - mid, mid * 2, mid * 2));

            if (total > 0) {
                RSVPStatus[] order = { RSVPStatus.ACCEPTED, RSVPStatus.MAYBE,
                                        RSVPStatus.DECLINED, RSVPStatus.PENDING };
                Color[] colors = { Theme.SUCCESS, Theme.WARNING, Theme.DANGER, Theme.TEXT_TERTIARY };
                float reveal = revealT;
                float start = 90f;
                for (int i = 0; i < order.length; i++) {
                    long c = counts.getOrDefault(order[i], 0L);
                    if (c <= 0) continue;
                    float extent = -(360f * c / total) * reveal;
                    boolean hover = (i == hoverSeg);
                    float expand = hover ? 6f : 0f;
                    float rOut = ring + expand;
                    float rIn  = rInner - (hover ? 2f : 0f);
                    Shape arc = donutSlice(cx, cy, rOut, rIn, start, extent);
                    g2.setColor(hover
                        ? brighten(colors[i], 0.12f)
                        : colors[i]);
                    g2.fill(arc);
                    start += extent;
                }
            }

            // Center text: confirmed (ACCEPTED) count
            long accepted = counts.getOrDefault(RSVPStatus.ACCEPTED, 0L);
            String hero = String.valueOf(accepted);
            String sub = "accepted";
            g2.setFont(Typography.NUMERAL.deriveFont((float) Math.min(48, d / 4f)));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(hero);
            int tx = cx - tw / 2;
            int ty = cy + fm.getAscent() / 2 - 6;
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.drawString(hero, tx, ty);

            g2.setFont(Typography.LABEL);
            FontMetrics fm2 = g2.getFontMetrics();
            int sw = fm2.stringWidth(sub);
            g2.setColor(Theme.TEXT_SECONDARY);
            Typography.drawTracked(g2, sub.toUpperCase(), Typography.LABEL, 0.06f,
                cx - sw / 2f, ty + 14);
        } finally {
            g2.dispose();
        }
    }

    private static Color brighten(Color c, float amount) {
        float a = Math.max(0f, Math.min(1f, amount));
        return new Color(
            Math.min(255, Math.round(c.getRed()   + (255 - c.getRed())   * a)),
            Math.min(255, Math.round(c.getGreen() + (255 - c.getGreen()) * a)),
            Math.min(255, Math.round(c.getBlue()  + (255 - c.getBlue())  * a)));
    }

    private static Shape donutSlice(float cx, float cy, float rOut, float rIn,
                                     float startDeg, float extentDeg) {
        java.awt.geom.Path2D.Float p = new java.awt.geom.Path2D.Float();
        Arc2D outerArc = new Arc2D.Float(cx - rOut, cy - rOut, rOut * 2, rOut * 2,
            startDeg, extentDeg, Arc2D.OPEN);
        Arc2D innerArc = new Arc2D.Float(cx - rIn, cy - rIn, rIn * 2, rIn * 2,
            startDeg + extentDeg, -extentDeg, Arc2D.OPEN);
        p.append(outerArc, false);
        p.append(innerArc, true);
        p.closePath();
        return p;
    }
}
