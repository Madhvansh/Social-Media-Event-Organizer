package com.eventorganizer.ui.components;

import com.eventorganizer.ui.fx.Animator;
import com.eventorganizer.ui.theme.Easing;
import com.eventorganizer.ui.theme.Gradient;
import com.eventorganizer.ui.theme.Motion;
import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * Horizontal segmented control with an animated pill that slides between
 * segments on selection. Optional count chips per segment. Replaces
 * {@code JTabbedPane} in most places where we want a cleaner, bolder look.
 *
 * <p>Use {@link #onChange(IntConsumer)} to observe index changes.
 */
public final class SegmentedControl extends JComponent {

    private final List<Segment> segments = new ArrayList<>();
    private int selectedIndex = 0;
    private float pillX = 0f;
    private float pillW = 0f;
    private float targetX = 0f;
    private float targetW = 0f;
    private Animator.Handle handle;
    private IntConsumer listener;
    private Accent accent = Accent.BRONZE;

    public enum Accent { BRONZE, AMETHYST }

    public SegmentedControl() {
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int idx = indexAt(e.getX());
                if (idx >= 0) setSelectedIndex(idx);
            }
        });
    }

    public SegmentedControl accent(Accent a) {
        this.accent = a;
        repaint();
        return this;
    }

    public SegmentedControl addSegment(String label) {
        return addSegment(label, -1);
    }

    public SegmentedControl addSegment(String label, int count) {
        segments.add(new Segment(label, count));
        revalidate();
        repaint();
        return this;
    }

    public SegmentedControl setCount(int index, int count) {
        if (index < 0 || index >= segments.size()) return this;
        segments.get(index).count = count;
        repaint();
        return this;
    }

    public int getSelectedIndex() { return selectedIndex; }

    public SegmentedControl setSelectedIndex(int idx) {
        if (idx < 0 || idx >= segments.size() || idx == selectedIndex) return this;
        selectedIndex = idx;
        animatePillTo(idx);
        repaint();
        if (listener != null) listener.accept(idx);
        return this;
    }

    public SegmentedControl onChange(IntConsumer listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(Typography.LABEL);
        int total = 12;
        int h = fm.getHeight() + 18;
        for (Segment s : segments) total += segmentWidth(fm, s) + 4;
        return new Dimension(total, h);
    }

    private int segmentWidth(FontMetrics fm, Segment s) {
        int w = fm.stringWidth(s.label) + 28;
        if (s.count >= 0) {
            w += fm.stringWidth(countStr(s.count)) + 16;
        }
        return w;
    }

    private String countStr(int c) {
        return c >= 100 ? "99+" : String.valueOf(c);
    }

    private int indexAt(int x) {
        FontMetrics fm = getFontMetrics(Typography.LABEL);
        int cx = 6;
        for (int i = 0; i < segments.size(); i++) {
            int w = segmentWidth(fm, segments.get(i));
            if (x >= cx && x < cx + w) return i;
            cx += w + 4;
        }
        return -1;
    }

    private Rectangle boundsOf(int index) {
        FontMetrics fm = getFontMetrics(Typography.LABEL);
        int cx = 6;
        for (int i = 0; i < segments.size(); i++) {
            int w = segmentWidth(fm, segments.get(i));
            if (i == index) return new Rectangle(cx, 6, w, getHeight() - 12);
            cx += w + 4;
        }
        return new Rectangle(cx, 6, 0, getHeight() - 12);
    }

    private void animatePillTo(int idx) {
        Rectangle r = boundsOf(idx);
        targetX = r.x;
        targetW = r.width;
        if (Motion.REDUCED) {
            pillX = targetX;
            pillW = targetW;
            repaint();
            return;
        }
        if (handle != null) handle.cancel();
        final float startX = pillX, startW = pillW;
        handle = Animator.tween(Motion.SHORT, Easing.EASE_OUT_CUBIC, t -> {
            pillX = startX + (targetX - startX) * (float) t;
            pillW = startW + (targetW - startW) * (float) t;
            repaint();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

            int w = getWidth(), h = getHeight();
            int arc = Radius.PILL;
            g2.setColor(Theme.BG_ELEVATED);
            g2.fillRoundRect(0, 0, w, h, arc, arc);
            g2.setColor(Theme.BORDER_SUBTLE);
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            if (pillW == 0 && !segments.isEmpty()) {
                Rectangle r = boundsOf(selectedIndex);
                pillX = r.x; pillW = r.width;
                targetX = pillX; targetW = pillW;
            }

            if (pillW > 0) {
                int py = 6;
                int ph = h - 12;
                g2.setPaint(accent == Accent.AMETHYST
                    ? Gradient.plumSweep((int) pillW, ph)
                    : Gradient.bronzeSweep((int) pillW, ph));
                g2.fillRoundRect((int) pillX, py, (int) pillW, ph, arc, arc);
            }

            FontMetrics fm = g2.getFontMetrics(Typography.LABEL);
            g2.setFont(Typography.LABEL);
            int cx = 6;
            for (int i = 0; i < segments.size(); i++) {
                Segment s = segments.get(i);
                int sw = segmentWidth(fm, s);
                boolean selected = (i == selectedIndex);
                Color fg = selected ? new Color(0x1B1612) : Theme.TEXT_SECONDARY;
                g2.setColor(fg);
                int tx = cx + 14;
                int ty = 6 + (h - 12 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(s.label, tx, ty);
                if (s.count >= 0) {
                    String cs = countStr(s.count);
                    int cwid = fm.stringWidth(cs);
                    int chipX = cx + sw - 14 - cwid - 10;
                    int chipY = 6 + (h - 12 - (fm.getHeight() + 2)) / 2;
                    int chipW = cwid + 10;
                    int chipH = fm.getHeight() + 2;
                    Color chipBg = selected
                        ? new Color(27, 22, 18, 120)
                        : Theme.BG_OVERLAY;
                    g2.setColor(chipBg);
                    g2.fillRoundRect(chipX, chipY, chipW, chipH, chipH, chipH);
                    g2.setColor(selected ? new Color(27, 22, 18) : Theme.TEXT_TERTIARY);
                    g2.drawString(cs, chipX + 5, chipY + fm.getAscent() + 1);
                }
                cx += sw + 4;
            }
        } finally {
            g2.dispose();
        }
    }

    private static final class Segment {
        final String label;
        int count;
        Segment(String label, int count) { this.label = label; this.count = count; }
    }
}
