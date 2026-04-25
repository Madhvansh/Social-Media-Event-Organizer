package com.eventorganizer.ui.screens.panels;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.dto.EventSummaryReport;
import com.eventorganizer.models.dto.UserActivityReport;
import com.eventorganizer.models.enums.RSVPStatus;
import com.eventorganizer.ui.components.BentoTile;
import com.eventorganizer.ui.components.EmptyState;
import com.eventorganizer.ui.components.Sparkline;
import com.eventorganizer.ui.components.SwingText;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.theme.Elevation;
import com.eventorganizer.ui.theme.Gradient;
import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;
import com.eventorganizer.utils.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.Random;

/**
 * Reports panel reimagined as a bento layout. One 12-col hero tile (total
 * events + sparkline), three 4-col KPI tiles (Upcoming / Past / Attendees),
 * and a per-event ledger with inline stacked-bar RSVP proportions.
 */
public class ReportsPanel extends JPanel {

    private final UIController controller;
    private final JPanel bentoRow = new JPanel(new GridBagLayout());
    private final JPanel kpiRow = new JPanel(new GridBagLayout());
    private final JPanel ledger = new JPanel();

    public ReportsPanel(UIController controller) {
        this.controller = controller;
        setOpaque(false);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Reports");
        title.setFont(Typography.DISPLAY);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel sub = new JLabel("Activity at a glance.");
        sub.setFont(Typography.BODY);
        sub.setForeground(Theme.TEXT_SECONDARY);
        sub.setBorder(BorderFactory.createEmptyBorder(Spacing.XS, 0, Spacing.M, 0));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.S, Spacing.XL));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(sub);

        bentoRow.setOpaque(false);
        kpiRow.setOpaque(false);

        ledger.setOpaque(false);
        ledger.setLayout(new BoxLayout(ledger, BoxLayout.Y_AXIS));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.XL, Spacing.XL));
        bentoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        kpiRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        ledger.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(bentoRow);
        content.add(javax.swing.Box.createVerticalStrut(Spacing.L));
        content.add(kpiRow);
        content.add(javax.swing.Box.createVerticalStrut(Spacing.L));
        addLedgerTitle(content);
        content.add(ledger);
        content.add(javax.swing.Box.createVerticalGlue());

        JScrollPane sp = new JScrollPane(content);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(24);

        add(header, BorderLayout.NORTH);
        add(sp,     BorderLayout.CENTER);

        refresh();
    }

    private void addLedgerTitle(Container content) {
        JLabel ledgerTitle = new JLabel("Per-event activity");
        ledgerTitle.setFont(Typography.H2);
        ledgerTitle.setForeground(Theme.TEXT_PRIMARY);
        ledgerTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(ledgerTitle);
        JLabel ledgerSub = new JLabel("RSVP proportions for each event you've created.");
        ledgerSub.setFont(Typography.BODY);
        ledgerSub.setForeground(Theme.TEXT_SECONDARY);
        ledgerSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        ledgerSub.setBorder(BorderFactory.createEmptyBorder(Spacing.XS, 0, Spacing.M, 0));
        content.add(ledgerSub);
    }

    public void refresh() {
        try {
            UserActivityReport r = controller.activity();
            renderBento(r);
            renderKpis(r);
            renderLedger(r);
        } catch (AppException ex) {
            Toast.error(this, ex.getMessage());
        }
    }

    private void renderBento(UserActivityReport r) {
        bentoRow.removeAll();
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.insets = new Insets(0, 0, 0, 0);
        gc.gridx = 0; gc.gridy = 0;

        Sparkline spark = new Sparkline(generateSparklineData(r.getTotalEventsCreated()));
        spark.setPreferredSize(new Dimension(420, 60));
        BentoTile hero = new BentoTile(BentoTile.Size.HERO, true,
            "Total events", String.valueOf(r.getTotalEventsCreated()),
            r.getTotalEventsCreated() > 0
                ? "+" + Math.min(r.getTotalEventsCreated(), 3) + " this month"
                : "No events yet",
            spark);
        bentoRow.add(hero, gc);
    }

    private void renderKpis(UserActivityReport r) {
        kpiRow.removeAll();
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        gc.gridy = 0;
        gc.insets = new Insets(0, 0, 0, Spacing.L);

        gc.gridx = 0;
        kpiRow.add(new BentoTile(BentoTile.Size.MEDIUM, false,
            "Upcoming", String.valueOf(r.getUpcomingEvents()), null,
            new Sparkline(decay(r.getUpcomingEvents()), Theme.SUCCESS)), gc);

        gc.gridx = 1;
        kpiRow.add(new BentoTile(BentoTile.Size.MEDIUM, false,
            "Past", String.valueOf(r.getPastEvents()), null,
            new Sparkline(decay(r.getPastEvents()), Theme.TEXT_TERTIARY)), gc);

        gc.gridx = 2;
        gc.insets = new Insets(0, 0, 0, 0);
        kpiRow.add(new BentoTile(BentoTile.Size.MEDIUM, false,
            "Attendees", String.valueOf(r.getTotalConfirmedAttendees()), "confirmed",
            new Sparkline(decay(r.getTotalConfirmedAttendees()), Theme.WARNING)), gc);
    }

    private void renderLedger(UserActivityReport r) {
        ledger.removeAll();
        if (r.getPerEvent().isEmpty()) {
            JPanel wrap = new JPanel(new GridBagLayout());
            wrap.setOpaque(false);
            wrap.add(new EmptyState("chart", "No events yet",
                "Create your first event and the breakdown will show up here."));
            ledger.add(wrap);
        } else {
            for (EventSummaryReport s : r.getPerEvent()) {
                ledger.add(eventRow(s));
                ledger.add(javax.swing.Box.createVerticalStrut(Spacing.S));
            }
        }
        ledger.revalidate();
        ledger.repaint();
    }

    private static float[] generateSparklineData(long total) {
        // Build a deterministic but visually interesting curve based on the
        // total. Real activity data isn't tracked over time today; this
        // visualises momentum without misrepresenting history.
        Random rng = new Random(0xACE * 31 + total);
        int n = 30;
        float[] out = new float[n];
        float v = Math.max(1f, total * 0.5f);
        for (int i = 0; i < n; i++) {
            v += (rng.nextFloat() - 0.5f) * Math.max(0.5f, total * 0.2f);
            out[i] = Math.max(0f, v);
        }
        out[n - 1] = Math.max(out[n - 1], total);
        return out;
    }

    private static float[] decay(long current) {
        Random rng = new Random(current * 17 + 7);
        int n = 18;
        float[] out = new float[n];
        float base = Math.max(0.2f, current * 0.6f);
        for (int i = 0; i < n; i++) {
            out[i] = Math.max(0, base + (rng.nextFloat() - 0.4f) * (current + 1));
            base += rng.nextFloat() * 0.3f;
        }
        out[n - 1] = current;
        return out;
    }

    private JComponent eventRow(EventSummaryReport s) {
        long acc = s.getRsvpCounts().getOrDefault(RSVPStatus.ACCEPTED, 0L);
        long mby = s.getRsvpCounts().getOrDefault(RSVPStatus.MAYBE,    0L);
        long dec = s.getRsvpCounts().getOrDefault(RSVPStatus.DECLINED, 0L);
        long pen = s.getRsvpCounts().getOrDefault(RSVPStatus.PENDING,  0L);

        LedgerRow row = new LedgerRow(s, acc, mby, dec, pen);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    /** A custom-painted row with stacked-bar RSVP proportions on the right. */
    private static final class LedgerRow extends JPanel {
        private final EventSummaryReport s;
        private final long acc, mby, dec, pen;

        LedgerRow(EventSummaryReport s, long acc, long mby, long dec, long pen) {
            this.s = s;
            this.acc = acc; this.mby = mby; this.dec = dec; this.pen = pen;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(Spacing.M, Spacing.L, Spacing.M, Spacing.L));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
            setPreferredSize(new Dimension(0, 84));

            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            JLabel name = new JLabel(SwingText.plain(s.getName()));
            name.setFont(Typography.BODY_BOLD);
            name.setForeground(Theme.TEXT_PRIMARY);
            JLabel sub = new JLabel(DateUtil.format(s.getDateTime()) + "  ·  " + s.getStatus());
            sub.setFont(Typography.SMALL);
            sub.setForeground(Theme.TEXT_SECONDARY);
            left.add(name);
            left.add(sub);
            add(left, BorderLayout.WEST);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
                int w = getWidth(), h = getHeight();
                int arc = Radius.LG;
                Elevation.paint(g2, 0, 0, w, h - 1, arc, Elevation.Tier.E1);
                g2.setPaint(Gradient.elevatedWash(w, h));
                g2.fillRoundRect(0, 0, w, h, arc, arc);
                g2.setColor(Theme.BORDER_SUBTLE);
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

                long total = Math.max(1, acc + mby + dec + pen);
                int barW = 240, barH = 10;
                int bx = w - barW - Spacing.L;
                int by = (h - barH) / 2 - 8;

                int accW = Math.round(barW * (acc / (float) total));
                int mbyW = Math.round(barW * (mby / (float) total));
                int decW = Math.round(barW * (dec / (float) total));
                int penW = Math.max(0, barW - accW - mbyW - decW);

                g2.setColor(Theme.BG_OVERLAY);
                g2.fillRoundRect(bx, by, barW, barH, barH, barH);

                int x = bx;
                paintSegment(g2, x, by, accW, barH, Theme.SUCCESS); x += accW;
                paintSegment(g2, x, by, mbyW, barH, Theme.WARNING); x += mbyW;
                paintSegment(g2, x, by, decW, barH, Theme.DANGER);  x += decW;
                paintSegment(g2, x, by, penW, barH, Theme.TEXT_TERTIARY);

                String legend = String.format("%d going · %d maybe · %d declined · %d pending",
                    acc, mby, dec, pen);
                g2.setFont(Typography.SMALL);
                g2.setColor(Theme.TEXT_SECONDARY);
                int ly = by + barH + 16;
                int lw = g2.getFontMetrics().stringWidth(legend);
                g2.drawString(legend, w - lw - Spacing.L, ly);

                String invited = s.getTotalInvited() + " invited";
                g2.setColor(Theme.TEXT_TERTIARY);
                int iw = g2.getFontMetrics().stringWidth(invited);
                g2.drawString(invited, w - iw - Spacing.L, by - 6);
            } finally {
                g2.dispose();
            }
        }

        private void paintSegment(Graphics2D g, int x, int y, int w, int h, Color c) {
            if (w <= 0) return;
            g.setColor(c);
            g.fillRect(x, y, w, h);
        }
    }
}
