package com.eventorganizer.ui.components;

import com.eventorganizer.models.Event;
import com.eventorganizer.ui.fx.Animator;
import com.eventorganizer.ui.theme.Easing;
import com.eventorganizer.ui.theme.Gradient;
import com.eventorganizer.ui.theme.Motion;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Vertical timeline layout for upcoming events. 2 px bronze-gradient spine on
 * the left; date anchors (bronze circles with day-of-month) at each event's
 * vertical position; event cards float to the right of the spine. A pulsing
 * bronze dot marks "today" if any event falls on today's date.
 *
 * <p>This is the signature visual of MyEvents — preserves chronology while
 * giving the app a memorable hero view.
 */
public final class TimelineSpine extends JPanel {

    private static final int SPINE_X = 44;
    private static final int ANCHOR_R = 12;

    private float pulse = 1f;
    private final Timer pulseTimer;

    public TimelineSpine(List<Event> upcoming, Consumer<Event> onOpen) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(Spacing.L, Spacing.S, Spacing.XL, Spacing.L));

        if (upcoming != null) {
            boolean first = true;
            for (Event e : upcoming) {
                if (!first) add(Box.createVerticalStrut(Spacing.S));
                TimelineRow row = new TimelineRow(e, onOpen);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                add(row);
                first = false;
            }
        }
        add(Box.createVerticalGlue());

        pulseTimer = new Timer(1000 / Motion.AMBIENT_FPS, e -> {
            long now = System.currentTimeMillis();
            pulse = 0.75f + 0.25f * (float) Math.sin(now / (Motion.PULSE_PERIOD_MS / (2f * Math.PI)));
            repaint(0, 0, SPINE_X + ANCHOR_R + 4, getHeight());
        });
        pulseTimer.setCoalesce(true);
        if (!Motion.REDUCED) pulseTimer.start();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (pulseTimer != null) pulseTimer.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int top = getInsets().top;
            int bot = getHeight() - getInsets().bottom;
            g2.setPaint(Gradient.bronzeSweep(2, bot - top));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
            g2.fillRect(SPINE_X - 1, top, 2, Math.max(0, bot - top));
        } finally {
            g2.dispose();
        }
    }

    /** A single row: date anchor on the spine + event card to the right. */
    public final class TimelineRow extends JComponent {
        private final Event event;
        private final TimelineCard card;
        private final boolean today;

        TimelineRow(Event e, Consumer<Event> onOpen) {
            this.event = e;
            this.today = e.getDateTime() != null
                && e.getDateTime().toLocalDate().equals(LocalDate.now());
            this.card = new TimelineCard(e, onOpen);
            setLayout(null);
            add(card);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension cp = card.getPreferredSize();
            return new Dimension(SPINE_X + 16 + cp.width, cp.height + 8);
        }

        @Override
        public void doLayout() {
            Dimension cp = card.getPreferredSize();
            card.setBounds(SPINE_X + 16, 0, Math.max(cp.width, getWidth() - SPINE_X - 16), cp.height);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

                int cy = getHeight() / 2;
                int cx = SPINE_X;

                // Anchor fill
                g2.setColor(Theme.BG_CANVAS);
                g2.fillOval(cx - ANCHOR_R - 2, cy - ANCHOR_R - 2, (ANCHOR_R + 2) * 2, (ANCHOR_R + 2) * 2);
                g2.setPaint(today ? Gradient.bronzeSweep(24, 24) : Theme.ACCENT_SOFT);
                g2.fillOval(cx - ANCHOR_R, cy - ANCHOR_R, ANCHOR_R * 2, ANCHOR_R * 2);
                g2.setColor(today ? Theme.ACCENT_HOVER : Theme.ACCENT);
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawOval(cx - ANCHOR_R, cy - ANCHOR_R, ANCHOR_R * 2 - 1, ANCHOR_R * 2 - 1);

                // Day number inside anchor
                int day = event.getDateTime() != null ? event.getDateTime().getDayOfMonth() : 1;
                g2.setFont(Typography.BODY_BOLD);
                java.awt.FontMetrics fm = g2.getFontMetrics();
                String ds = String.valueOf(day);
                int dw = fm.stringWidth(ds);
                g2.setColor(today ? new Color(0x1B1612) : Theme.TEXT_PRIMARY);
                g2.drawString(ds, cx - dw / 2, cy + fm.getAscent() / 2 - 2);

                // Today pulse
                if (today && !Motion.REDUCED) {
                    float r = ANCHOR_R + 6f * pulse;
                    Color c = new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(),
                        Theme.ACCENT.getBlue(), (int) (60 * (1.2f - pulse)));
                    g2.setColor(c);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));
                }

                // Month label below anchor
                g2.setFont(Typography.SMALL);
                java.awt.FontMetrics fm2 = g2.getFontMetrics();
                String mo = event.getDateTime() == null ? ""
                    : event.getDateTime().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();
                int mw = fm2.stringWidth(mo);
                g2.setColor(Theme.TEXT_SECONDARY);
                g2.drawString(mo, cx - mw / 2, cy + ANCHOR_R + fm2.getAscent() + 3);
            } finally {
                g2.dispose();
            }
        }
    }

    /** Inline event card with hover lift. Delegates to the canonical EventCard renderer. */
    public static final class TimelineCard extends JPanel {
        private final EventCard inner;
        private final Animator.Handle[] handle = new Animator.Handle[1];
        private float lift = 0f;

        TimelineCard(Event e, Consumer<Event> onOpen) {
            super(new java.awt.BorderLayout());
            setOpaque(false);
            inner = new EventCard(e, () -> onOpen.accept(e), EventCard.Variant.OWNED);
            add(inner, java.awt.BorderLayout.CENTER);

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent ev) { animate(1f); }
                @Override public void mouseExited(java.awt.event.MouseEvent ev)  { animate(0f); }
            });
        }

        private void animate(float target) {
            if (Motion.REDUCED) { lift = target; repaint(); return; }
            if (handle[0] != null) handle[0].cancel();
            final float start = lift;
            handle[0] = Animator.tween(Motion.SHORT, Easing.EASE_OUT_CUBIC, t -> {
                lift = start + (target - start) * (float) t;
                repaint();
            });
        }

        @Override
        public Dimension getPreferredSize() { return inner.getPreferredSize(); }
    }
}
