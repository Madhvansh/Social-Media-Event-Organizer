package com.eventorganizer.ui.components;

import com.eventorganizer.ui.theme.GrainPainter;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;

/**
 * A JPanel whose canvas is painted with the warm BG_PRIMARY (or caller-provided)
 * surface color plus a subtle grain overlay. Canvas panels (auth, dashboard)
 * and large dialog content panes extend this so the warm tone is uniform and
 * 8-bit banding is defeated.
 */
public class SurfacePanel extends JPanel {

    private final Color surface;
    private final boolean grain;

    public SurfacePanel() {
        this(Theme.BG_PRIMARY, true);
    }

    public SurfacePanel(LayoutManager layout) {
        this(layout, Theme.BG_PRIMARY, true);
    }

    public SurfacePanel(Color surface, boolean grain) {
        super();
        this.surface = surface;
        this.grain = grain;
        setOpaque(true);
        setBackground(surface);
    }

    public SurfacePanel(LayoutManager layout, Color surface, boolean grain) {
        super(layout);
        this.surface = surface;
        this.grain = grain;
        setOpaque(true);
        setBackground(surface);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setColor(surface);
            g2.fillRect(0, 0, getWidth(), getHeight());
            if (grain) {
                GrainPainter.paintGrain(g2, getWidth(), getHeight());
            }
        } finally {
            g2.dispose();
        }
    }
}
