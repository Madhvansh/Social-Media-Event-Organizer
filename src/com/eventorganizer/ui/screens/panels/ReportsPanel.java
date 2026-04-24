package com.eventorganizer.ui.screens.panels;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.dto.EventSummaryReport;
import com.eventorganizer.models.dto.UserActivityReport;
import com.eventorganizer.models.enums.RSVPStatus;
import com.eventorganizer.ui.components.EmptyState;
import com.eventorganizer.ui.components.SwingText;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.SoftBorder;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.utils.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;

public class ReportsPanel extends JPanel {

    private final UIController controller;
    private final JPanel kpiRow = new JPanel(new GridLayout(1, 4, Spacing.M, 0));
    private final JPanel perEventList = new JPanel();

    public ReportsPanel(UIController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PRIMARY);

        JLabel title = new JLabel("Reports");
        title.setFont(Theme.FONT_DISPLAY);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(Spacing.L, Spacing.XL, Spacing.S, Spacing.XL));

        kpiRow.setOpaque(false);
        kpiRow.setBorder(BorderFactory.createEmptyBorder(0, Spacing.L, Spacing.M, Spacing.L));

        perEventList.setLayout(new BoxLayout(perEventList, BoxLayout.Y_AXIS));
        perEventList.setBackground(Theme.BG_PRIMARY);
        perEventList.setBorder(BorderFactory.createEmptyBorder(Spacing.S, Spacing.L, Spacing.L, Spacing.L));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(kpiRow, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(perEventList), BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        try {
            UserActivityReport r = controller.activity();

            kpiRow.removeAll();
            kpiRow.add(kpiTile("Total",     String.valueOf(r.getTotalEventsCreated()), Theme.ACCENT));
            kpiRow.add(kpiTile("Upcoming",  String.valueOf(r.getUpcomingEvents()),     Theme.SUCCESS));
            kpiRow.add(kpiTile("Past",      String.valueOf(r.getPastEvents()),         Theme.TEXT_MUTED));
            kpiRow.add(kpiTile("Attendees", String.valueOf(r.getTotalConfirmedAttendees()), Theme.WARNING));

            perEventList.removeAll();
            if (r.getPerEvent().isEmpty()) {
                EmptyState empty = new EmptyState("◊", "No events yet",
                    "Create your first event and the breakdown will show up here.");
                empty.setAlignmentX(Component.CENTER_ALIGNMENT);
                JPanel wrap = new JPanel(new java.awt.GridBagLayout());
                wrap.setOpaque(false);
                wrap.add(empty);
                wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
                perEventList.add(wrap);
            } else {
                for (EventSummaryReport s : r.getPerEvent()) {
                    perEventList.add(eventRow(s));
                    perEventList.add(Box.createVerticalStrut(Spacing.S));
                }
            }
            perEventList.add(Box.createVerticalGlue());
            kpiRow.revalidate(); kpiRow.repaint();
            perEventList.revalidate(); perEventList.repaint();
        } catch (AppException ex) {
            Toast.error(this, ex.getMessage());
        }
    }

    private JPanel kpiTile(String label, String value, java.awt.Color accent) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Theme.BG_ELEVATED);
        p.setBorder(BorderFactory.createCompoundBorder(
            SoftBorder.of(Radius.LG, Theme.BORDER, 1),
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, accent),
                BorderFactory.createEmptyBorder(Spacing.M, Spacing.L, Spacing.M, Spacing.L))));
        JLabel v = new JLabel(value);
        v.setFont(Theme.FONT_DISPLAY);
        v.setForeground(Theme.TEXT_PRIMARY);
        JLabel k = new JLabel(label);
        k.setFont(Theme.FONT_SMALL);
        k.setForeground(Theme.TEXT_MUTED);
        p.add(v); p.add(k);
        return p;
    }

    private JPanel eventRow(EventSummaryReport s) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Theme.BG_ELEVATED);
        row.setBorder(SoftBorder.of(Radius.MD, Theme.BORDER, 1,
            new Insets(Spacing.M, Spacing.L, Spacing.M, Spacing.L)));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel name = new JLabel(SwingText.plain(s.getName()));
        name.setFont(Theme.FONT_BODY_BOLD);
        name.setForeground(Theme.TEXT_PRIMARY);
        JLabel sub = new JLabel(DateUtil.format(s.getDateTime()) + "  •  " + s.getStatus());
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(Theme.TEXT_MUTED);
        left.add(name); left.add(sub);

        JLabel counts = new JLabel(String.format(
            "A:%d  D:%d  M:%d  P:%d  (%d invited)",
            s.getRsvpCounts().getOrDefault(RSVPStatus.ACCEPTED, 0L),
            s.getRsvpCounts().getOrDefault(RSVPStatus.DECLINED, 0L),
            s.getRsvpCounts().getOrDefault(RSVPStatus.MAYBE,    0L),
            s.getRsvpCounts().getOrDefault(RSVPStatus.PENDING,  0L),
            s.getTotalInvited()));
        counts.setFont(Theme.FONT_SMALL);
        counts.setForeground(Theme.TEXT_MUTED);

        row.add(left, BorderLayout.WEST);
        row.add(counts, BorderLayout.EAST);
        return row;
    }
}
