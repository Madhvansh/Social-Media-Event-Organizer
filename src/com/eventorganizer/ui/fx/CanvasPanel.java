package com.eventorganizer.ui.fx;

import com.eventorganizer.ui.theme.Gradient;
import com.eventorganizer.ui.theme.GrainPainter;
import com.eventorganizer.ui.theme.Motion;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;

/**
 * Root canvas panel. Replaces the old {@code SurfacePanel} and any sibling
 * {@code MeshBackdrop} child — the aurora-mesh blobs and grain are rendered
 * inline in this component's {@link #paintComponent} so every animation frame
 * triggers a clean parent → children paint chain. Content is added directly
 * with the caller's layout manager.
 *
 * <p>This eliminates the strobing seen when an opaque sibling component had
 * its own {@code repaint()} call: under {@link javax.swing.OverlayLayout},
 * Swing's repaint manager would only refresh the bottom layer's bounds and
 * temporarily wipe the content layer above. With the mesh painted by the
 * parent itself, the content always paints on top of the freshly-drawn mesh.
 *
 * <p>The 24 fps drift timer pauses while the window is hidden or unfocused,
 * keeping idle CPU near zero. {@link Motion#REDUCED} freezes the mesh to a
 * single static frame.
 */
public final class CanvasPanel extends JPanel {

    private static final int BLOBS = 4;
    private static final double[] PERIODS_X = { 45_000, 60_000, 80_000, 120_000 };
    private static final double[] PERIODS_Y = { 60_000, 80_000, 50_000, 95_000 };
    private static final Color[] TINTS = {
        new Color(0xC8915F),
        new Color(0x9B7FB5),
        new Color(0xA87848),
        new Color(0x5F4A78),
    };
    private static final int[] ALPHAS = { 140, 110, 90, 70 };

    private final Timer ambient;
    private BufferedImage buffer;
    private int bufW, bufH;
    private final long startNanos = System.nanoTime();

    public CanvasPanel(LayoutManager layout) {
        super(layout);
        setOpaque(true);
        setBackground(Theme.BG_CANVAS);
        setDoubleBuffered(true);

        ambient = new Timer(1000 / Motion.AMBIENT_FPS, e -> repaint());
        ambient.setCoalesce(true);

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
        if (Motion.REDUCED) { ambient.stop(); return; }
        Window w = SwingUtilities.getWindowAncestor(this);
        boolean active = isShowing() && (w == null || w.isFocused() || w.isActive());
        if (active && !ambient.isRunning()) ambient.start();
        else if (!active && ambient.isRunning()) ambient.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // BG_CANVAS fill

        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        int lowW = Math.max(1, w / 2);
        int lowH = Math.max(1, h / 2);
        if (buffer == null || bufW != lowW || bufH != lowH) {
            buffer = new BufferedImage(lowW, lowH, BufferedImage.TYPE_INT_RGB);
            bufW = lowW;
            bufH = lowH;
        }

        renderLowRes(buffer);

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(buffer, 0, 0, w, h, null);
            GrainPainter.paintGrain(g2, w, h, 0.035f);
        } finally {
            g2.dispose();
        }
    }

    private void renderLowRes(BufferedImage img) {
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Theme.BG_CANVAS);
            g.fillRect(0, 0, img.getWidth(), img.getHeight());

            double elapsedMs = Motion.REDUCED ? 0.0 : (System.nanoTime() - startNanos) / 1_000_000.0;
            int w = img.getWidth(), h = img.getHeight();

            for (int i = 0; i < BLOBS; i++) {
                double phaseX = (elapsedMs / PERIODS_X[i]) * 2 * Math.PI + i * 0.7;
                double phaseY = (elapsedMs / PERIODS_Y[i]) * 2 * Math.PI + i * 1.3;
                float cx = (float) (w * (0.5 + 0.35 * Math.sin(phaseX)));
                float cy = (float) (h * (0.5 + 0.35 * Math.cos(phaseY)));
                float radius = (float) (Math.max(w, h) * 0.65);
                g.setPaint(Gradient.blob(cx, cy, radius, TINTS[i], ALPHAS[i]));
                g.fillRect(0, 0, w, h);
            }
        } finally {
            g.dispose();
        }
    }
}
