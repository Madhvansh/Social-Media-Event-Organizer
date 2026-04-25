package com.eventorganizer.ui.components;

import com.eventorganizer.ui.theme.Elevation;
import com.eventorganizer.ui.theme.Gradient;
import com.eventorganizer.ui.theme.Radius;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * KPI tile for the Reports bento layout. Renders a label (tracked uppercase),
 * a huge numeral, an optional delta line, and an optional child visualisation
 * (sparkline, donut, or custom component) sitting below.
 */
public final class BentoTile extends JPanel {

    public enum Size { SMALL, MEDIUM, HERO }

    private final Size size;
    private final boolean accent;

    public BentoTile(Size size, boolean accent, String label, String value,
                     String delta, JComponent child) {
        super(new BorderLayout());
        this.size = size;
        this.accent = accent;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.XL, Spacing.XL));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setAlignmentX(Component.LEFT_ALIGNMENT);

        TrackedLabel lbl = new TrackedLabel(label.toUpperCase(), Typography.LABEL,
            Theme.TEXT_SECONDARY, 0.08f);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel num = new JLabel(value);
        num.setFont(size == Size.HERO ? Typography.NUMERAL
            : size == Size.MEDIUM ? Typography.DISPLAY
            : Typography.H1);
        num.setForeground(Theme.TEXT_PRIMARY);
        num.setAlignmentX(Component.LEFT_ALIGNMENT);
        num.setBorder(BorderFactory.createEmptyBorder(Spacing.XS, 0, 0, 0));

        text.add(lbl);
        text.add(num);

        if (delta != null && !delta.isEmpty()) {
            JLabel d = new JLabel(delta);
            d.setFont(Typography.SMALL);
            d.setForeground(accent ? Theme.ACCENT : Theme.TEXT_TERTIARY);
            d.setAlignmentX(Component.LEFT_ALIGNMENT);
            d.setBorder(BorderFactory.createEmptyBorder(Spacing.XS, 0, 0, 0));
            text.add(d);
        }

        add(text, BorderLayout.NORTH);

        if (child != null) {
            child.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(child, BorderLayout.SOUTH);
        } else {
            add(Box.createVerticalStrut(Spacing.S), BorderLayout.SOUTH);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        int minH = size == Size.HERO ? 200 : size == Size.MEDIUM ? 140 : 120;
        if (d.height < minH) d.height = minH;
        return d;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int arc = Radius.XL;

            // shadow
            Elevation.paint(g2, 0, 0, w, h, arc, Elevation.Tier.E1);

            // fill
            g2.setPaint(Gradient.elevatedWash(w, h));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // border (accent glow on featured tiles)
            g2.setColor(accent ? Theme.BORDER_GLOW : Theme.BORDER_SUBTLE);
            g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

            if (accent) {
                g2.setColor(new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(),
                    Theme.ACCENT.getBlue(), 40));
                g2.fillRoundRect(0, 0, 4, h, 4, 4);
            }
        } finally {
            g2.dispose();
        }
    }
}
