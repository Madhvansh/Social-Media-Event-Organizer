package com.eventorganizer.ui.fx;

import com.eventorganizer.ui.theme.Motion;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.Random;

/**
 * Sparse particle field for the AuthScreen. ~30 bronze dots drifting slowly;
 * lines drawn between any two dots within {@link #LINK_DISTANCE} px, alpha
 * scaled by proximity. Pauses when unfocused or hidden.
 */
public final class Constellation extends JComponent {

    private static final int DOTS = 34;
    private static final float LINK_DISTANCE = 120f;
    private static final float DOT_RADIUS_MIN = 1.1f;
    private static final float DOT_RADIUS_MAX = 2.3f;
    private static final float MAX_SPEED = 0.18f; // px/frame

    private final Particle[] particles = new Particle[DOTS];
    private final Timer timer;
    private final Random rng = new Random(0xA11E5E5);
    private boolean seeded = false;

    public Constellation() {
        setOpaque(false);
        timer = new Timer(1000 / Motion.AMBIENT_FPS, e -> { step(); repaint(); });
        timer.setCoalesce(true);
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) updateRunState();
        });
        SwingUtilities.invokeLater(this::attachFocusListener);
    }

    private void attachFocusListener() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (w == null) return;
        w.addWindowFocusListener(new WindowFocusListener() {
            @Override public void windowGainedFocus(WindowEvent e) { updateRunState(); }
            @Override public void windowLostFocus(WindowEvent e)   { updateRunState(); }
        });
    }

    private void updateRunState() {
        if (Motion.REDUCED) { timer.stop(); repaint(); return; }
        Window w = SwingUtilities.getWindowAncestor(this);
        boolean active = isShowing() && (w == null || w.isFocused() || w.isActive());
        if (active && !timer.isRunning()) timer.start();
        else if (!active && timer.isRunning()) timer.stop();
    }

    private void seed() {
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());
        for (int i = 0; i < DOTS; i++) {
            Particle p = new Particle();
            p.x = rng.nextFloat() * w;
            p.y = rng.nextFloat() * h;
            p.vx = (rng.nextFloat() * 2f - 1f) * MAX_SPEED;
            p.vy = (rng.nextFloat() * 2f - 1f) * MAX_SPEED;
            p.r = DOT_RADIUS_MIN + rng.nextFloat() * (DOT_RADIUS_MAX - DOT_RADIUS_MIN);
            particles[i] = p;
        }
        seeded = true;
    }

    private void step() {
        if (!seeded || getWidth() == 0 || getHeight() == 0) return;
        int w = getWidth(), h = getHeight();
        for (Particle p : particles) {
            p.x += p.vx; p.y += p.vy;
            if (p.x < -5)    { p.x = w + 5; }
            if (p.x > w + 5) { p.x = -5; }
            if (p.y < -5)    { p.y = h + 5; }
            if (p.y > h + 5) { p.y = -5; }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(800, 600);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!seeded) seed();
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color line = new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(),
                Theme.ACCENT.getBlue(), 55);
            Color dot = new Color(Theme.ACCENT_HOVER.getRed(), Theme.ACCENT_HOVER.getGreen(),
                Theme.ACCENT_HOVER.getBlue(), 200);

            for (int i = 0; i < DOTS; i++) {
                for (int j = i + 1; j < DOTS; j++) {
                    Particle a = particles[i], b = particles[j];
                    float dx = a.x - b.x, dy = a.y - b.y;
                    float d = (float) Math.sqrt(dx * dx + dy * dy);
                    if (d > LINK_DISTANCE) continue;
                    float t = 1f - d / LINK_DISTANCE;
                    int alpha = Math.round(t * t * 55);
                    g2.setColor(new Color(line.getRed(), line.getGreen(), line.getBlue(), alpha));
                    g2.draw(new Line2D.Float(a.x, a.y, b.x, b.y));
                }
            }
            g2.setColor(dot);
            for (Particle p : particles) {
                g2.fill(new Ellipse2D.Float(p.x - p.r, p.y - p.r, p.r * 2, p.r * 2));
            }
        } finally {
            g2.dispose();
        }
    }

    private static final class Particle {
        float x, y, vx, vy, r;
    }
}
