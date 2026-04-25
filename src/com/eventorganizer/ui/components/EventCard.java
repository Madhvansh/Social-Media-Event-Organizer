package com.eventorganizer.ui.components;

import com.eventorganizer.models.Event;
import com.eventorganizer.models.enums.EventStatus;
import com.eventorganizer.models.enums.EventType;
import com.eventorganizer.ui.fx.Animator;
import com.eventorganizer.ui.theme.Easing;
import com.eventorganizer.ui.theme.Elevation;
import com.eventorganizer.ui.theme.Gradient;
import com.eventorganizer.ui.theme.Iconography;
import com.eventorganizer.ui.theme.Motion;
import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Editorial event card. Three rows:
 * <ol>
 *   <li>Date block (day-of-month in 28 pt, month LABEL tracked uppercase) · title + badges</li>
 *   <li>Description (2-line truncate)</li>
 *   <li>Location glyph + location · RSVP mini-indicator (three proportional dots)</li>
 * </ol>
 *
 * <p>Hover lifts the card by 2 px and fades in an accent border glow.
 * Clicking anywhere fires {@code onOpen}.
 *
 * <p>Two variants: {@link Variant#OWNED} (bronze accent, for My Events) and
 * {@link Variant#INCOMING} (amethyst accent, for Discover / invitations).
 */
public final class EventCard extends JPanel {

    public enum Variant { OWNED, INCOMING }

    private static final int MIN_HEIGHT = 128;

    private final Event event;
    private final Variant variant;
    private float hoverT = 0f;
    private Animator.Handle handle;

    public EventCard(Event event, Runnable onOpen) {
        this(event, onOpen, Variant.OWNED);
    }

    public EventCard(Event event, Runnable onOpen, Variant variant) {
        this.event = event;
        this.variant = variant;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(
            Spacing.L, Spacing.XL, Spacing.L, Spacing.XL));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setLayout(null);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { animate(1f); }
            @Override public void mouseExited(MouseEvent e)  { animate(0f); }
            @Override public void mouseClicked(MouseEvent e) {
                if (onOpen != null) onOpen.run();
            }
        });
    }

    private void animate(float target) {
        if (Motion.REDUCED) { hoverT = target; repaint(); return; }
        if (handle != null) handle.cancel();
        final float start = hoverT;
        handle = Animator.tween(Motion.SHORT, Easing.EASE_OUT_CUBIC, t -> {
            hoverT = start + (target - start) * (float) t;
            repaint();
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Math.max(420, getParent() != null ? getParent().getWidth() : 500),
            MIN_HEIGHT);
    }

    @Override
    public Dimension getMinimumSize() { return new Dimension(320, MIN_HEIGHT); }

    @Override
    public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, MIN_HEIGHT); }

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);

            int lift = Math.round(2f * hoverT);
            int arc = Radius.LG;

            Elevation.paint(g2, 0, -lift, w, h - 1, arc,
                hoverT > 0.05f ? Elevation.Tier.E2 : Elevation.Tier.E1);

            g2.setPaint(Gradient.elevatedWash(w, h));
            g2.fillRoundRect(0, -lift, w, h, arc, arc);

            Color accent = variant == Variant.INCOMING ? Theme.ACCENT2 : Theme.ACCENT;
            float borderAlpha = 0.35f + 0.60f * hoverT;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g2.setColor(hoverT > 0.05f
                ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(),
                    Math.round(255 * borderAlpha))
                : Theme.BORDER_SUBTLE);
            g2.drawRoundRect(0, -lift, w - 1, h - 1, arc, arc);

            // Left accent stripe (color-coded variant)
            g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80));
            g2.fillRoundRect(0, -lift, 4, h, 4, 4);

            drawContent(g2, w, h - 1, lift);
        } finally {
            g2.dispose();
        }
    }

    private void drawContent(Graphics2D g, int w, int h, int lift) {
        int pad = Spacing.XL;
        int y = pad - lift;

        // --- Date block (left) ---
        int day = event.getDateTime() != null ? event.getDateTime().getDayOfMonth() : 0;
        String mo = event.getDateTime() == null ? "" :
            event.getDateTime().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();

        int dateX = pad;
        g.setFont(Typography.DISPLAY.deriveFont(28f));
        FontMetrics fmDay = g.getFontMetrics();
        g.setColor(Theme.TEXT_PRIMARY);
        String dayStr = String.format("%02d", day);
        g.drawString(dayStr, dateX, y + fmDay.getAscent());

        g.setFont(Typography.LABEL);
        FontMetrics fmMo = g.getFontMetrics();
        g.setColor(Theme.TEXT_SECONDARY);
        Typography.drawTracked(g, mo, Typography.LABEL, 0.06f,
            dateX, y + fmDay.getAscent() + fmMo.getAscent() + 2);

        // --- Title + badges (right of date block) ---
        int textX = dateX + fmDay.stringWidth("00") + Spacing.XL;
        int textW = w - textX - pad;

        g.setFont(Typography.H2);
        FontMetrics fmT = g.getFontMetrics();
        g.setColor(Theme.TEXT_PRIMARY);
        String title = truncate(g, event.getName(), textW - 110);
        int titleY = y + fmT.getAscent();
        g.drawString(title, textX, titleY);

        // Badge row below title
        int bX = textX;
        int bY = titleY + 8;
        bX += drawBadge(g, bX, bY, event.getType().name(),
            event.getType() == EventType.PUBLIC ? Theme.ACCENT : Theme.ACCENT2,
            event.getType() == EventType.PUBLIC ? Theme.ACCENT_SOFT : Theme.ACCENT2_SOFT);
        bX += 6;
        drawStatusBadge(g, bX, bY);

        // --- Description (middle row) ---
        int descY = bY + 26;
        g.setFont(Typography.BODY);
        FontMetrics fmB = g.getFontMetrics();
        g.setColor(Theme.TEXT_SECONDARY);
        String desc = event.getDescription() == null ? "" : event.getDescription().replaceAll("\\s+", " ");
        String descT = truncate(g, desc, textW);
        if (!descT.isEmpty()) {
            g.drawString(descT, textX, descY + fmB.getAscent());
        }

        // --- Location + RSVP mini (bottom) ---
        int footerY = h - pad - 2 - lift;
        g.setColor(Theme.TEXT_TERTIARY);
        Iconography.paint(g, "pin", textX, footerY - 12, 14f, Theme.TEXT_TERTIARY);
        g.setFont(Typography.SMALL);
        FontMetrics fmS = g.getFontMetrics();
        String loc = event.getLocation() == null || event.getLocation().isEmpty()
            ? "—" : event.getLocation();
        g.drawString(loc, textX + 18, footerY);

        // RSVP mini-indicator
        drawRsvpMini(g, w - pad - 84, footerY - 10);
    }

    private int drawBadge(Graphics2D g, int x, int y, String text, Color fg, Color bg) {
        g.setFont(Typography.LABEL);
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(text) + 14;
        int h = fm.getHeight() + 2;
        g.setColor(bg);
        g.fillRoundRect(x, y, w, h, Radius.SM * 2, Radius.SM * 2);
        g.setColor(fg);
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(x, y, w - 1, h - 1, Radius.SM * 2, Radius.SM * 2);
        g.drawString(text, x + 7, y + fm.getAscent() + 1);
        return w;
    }

    private void drawStatusBadge(Graphics2D g, int x, int y) {
        String label;
        Color fg, bg;
        if (event.getStatus() == EventStatus.CANCELLED) {
            label = "CANCELLED"; fg = Theme.DANGER; bg = Theme.DANGER_SOFT;
        } else if (event.isPast()) {
            label = "PAST"; fg = Theme.TEXT_TERTIARY; bg = Theme.BG_OVERLAY;
        } else {
            label = "UPCOMING"; fg = Theme.SUCCESS; bg = Theme.SUCCESS_SOFT;
        }
        drawBadge(g, x, y, label, fg, bg);
    }

    private void drawRsvpMini(Graphics2D g, int x, int y) {
        long acc = count(event, com.eventorganizer.models.enums.RSVPStatus.ACCEPTED);
        long mby = count(event, com.eventorganizer.models.enums.RSVPStatus.MAYBE);
        long dec = count(event, com.eventorganizer.models.enums.RSVPStatus.DECLINED);
        long total = Math.max(1, acc + mby + dec);

        int w = 80, barH = 6;
        int bx = x, by = y + 4;
        g.setColor(Theme.BG_OVERLAY);
        g.fillRoundRect(bx, by, w, barH, barH, barH);

        int accW = Math.round(w * (acc / (float) total));
        int mbyW = Math.round(w * (mby / (float) total));
        int decW = Math.max(0, w - accW - mbyW);

        int cx = bx;
        g.setColor(Theme.SUCCESS); g.fillRoundRect(cx, by, accW, barH, barH, barH);
        cx += accW;
        g.setColor(Theme.WARNING); g.fillRect(cx, by, mbyW, barH);
        cx += mbyW;
        g.setColor(Theme.DANGER);  g.fillRect(cx, by, decW, barH);

        g.setFont(Typography.MONO);
        g.setColor(Theme.TEXT_TERTIARY);
        String caption = acc + " going";
        g.drawString(caption, bx, by - 4);
    }

    private long count(Event e, com.eventorganizer.models.enums.RSVPStatus s) {
        long c = 0;
        for (com.eventorganizer.models.Invitation i : e.getInvitations()) {
            if (i.getStatus() == s) c++;
        }
        return c;
    }

    private String truncate(Graphics2D g, String text, int maxPx) {
        if (text == null) return "";
        FontMetrics fm = g.getFontMetrics();
        if (fm.stringWidth(text) <= maxPx) return text;
        String dots = "…";
        int dotsW = fm.stringWidth(dots);
        int end = text.length();
        while (end > 0 && fm.stringWidth(text.substring(0, end)) + dotsW > maxPx) end--;
        return text.substring(0, Math.max(0, end)) + dots;
    }

    /** Escape hatch for anyone that still holds onto the component instead of repainting. */
    @SuppressWarnings("unused")
    private JComponent self() { return this; }
}
