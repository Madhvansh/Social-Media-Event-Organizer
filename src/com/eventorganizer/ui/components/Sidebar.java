package com.eventorganizer.ui.components;

import com.eventorganizer.ui.controllers.UIController;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Left NavRail. Each item: glyph icon, label, optional unread count badge.
 * The active item has a 4 px bronze-sweep rail on the left and a soft E_GLOW
 * halo that slides smoothly when selection changes.
 *
 * <p>Keeps the original {@code Sidebar} public API — same constructor and
 * {@link #refresh}/{@link #setActive}/{@link #activeKey} methods — so
 * DashboardScreen doesn't need changes.
 */
public class Sidebar extends JPanel {

    private final Map<String, NavItem> items = new LinkedHashMap<>();
    private String active;
    private final JPanel stack = new JPanel();
    private final Rail rail = new Rail();
    private final JComponent stackLayer;

    public Sidebar(UIController controller, Consumer<String> onSelect) {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Theme.BG_ELEVATED);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_SUBTLE));
        setPreferredSize(new Dimension(240, 0));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.L, Spacing.XL));

        TrackedLabel brand = new TrackedLabel("ATELIER", Typography.LABEL.deriveFont(11f),
            Theme.ACCENT, 0.12f);
        javax.swing.JLabel name = new javax.swing.JLabel("Event Organizer");
        name.setFont(Typography.H2);
        name.setForeground(Theme.TEXT_PRIMARY);
        JPanel hs = new JPanel();
        hs.setOpaque(false);
        hs.setLayout(new BoxLayout(hs, BoxLayout.Y_AXIS));
        brand.setAlignmentX(LEFT_ALIGNMENT);
        name.setAlignmentX(LEFT_ALIGNMENT);
        hs.add(brand);
        hs.add(Box.createVerticalStrut(2));
        hs.add(name);
        header.add(hs, BorderLayout.WEST);

        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setBorder(BorderFactory.createEmptyBorder(Spacing.L, Spacing.M, Spacing.L, Spacing.M));

        addItem("events",        "My Events",     "calendar", () -> 0, onSelect);
        addItem("discover",      "Discover",      "compass",  () -> controller.eventsInvitedTo().size(), onSelect);
        addItem("friends",       "Friends",       "users",    () -> controller.incomingReqs().size(), onSelect);
        addItem("notifications", "Notifications", "bell",     controller::unreadCount, onSelect);
        addItem("reports",       "Reports",       "chart",    () -> 0, onSelect);

        // Stack content; rail is painted directly through the stack's overlay.
        stackLayer = wrapWithRail(stack);

        add(header, BorderLayout.NORTH);
        add(stackLayer, BorderLayout.CENTER);

        JPanel filler = new JPanel();
        filler.setOpaque(false);
        add(filler, BorderLayout.SOUTH);

        setActive("events");
    }

    private JComponent wrapWithRail(JPanel inner) {
        JPanel overlay = new JPanel() {
            @Override
            protected void paintChildren(Graphics g) {
                super.paintChildren(g);
                rail.paintInto((Graphics2D) g, getHeight());
            }
        };
        overlay.setOpaque(false);
        overlay.setLayout(new BorderLayout());
        overlay.add(inner, BorderLayout.CENTER);
        return overlay;
    }

    private void addItem(String key, String label, String icon,
                         Supplier<Integer> count, Consumer<String> onSelect) {
        NavItem item = new NavItem(key, label, icon, count);
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                setActive(key);
                onSelect.accept(key);
            }
        });
        items.put(key, item);
        stack.add(item);
        stack.add(Box.createVerticalStrut(2));
    }

    public void setActive(String key) {
        if (key == null || !items.containsKey(key)) return;
        String prev = active;
        active = key;
        for (Map.Entry<String, NavItem> e : items.entrySet()) {
            e.getValue().setActive(e.getKey().equals(key));
        }
        if (prev != null && !prev.equals(key)) {
            rail.slideTo(items.get(key), stackLayer);
        } else {
            rail.snapTo(items.get(key));
        }
        repaint();
    }

    public void refresh(UIController controller) {
        for (NavItem item : items.values()) item.refresh();
    }

    public String activeKey() { return active; }

    /** Animated 4 px gradient rail that tracks the active NavItem. */
    private static final class Rail {
        float y = 0f, h = 0f;
        float targetY = 0f, targetH = 0f;
        Animator.Handle handle;

        void snapTo(NavItem n) {
            if (n == null) return;
            java.awt.Rectangle b = n.getBounds();
            y = b.y + 6; targetY = y;
            h = b.height - 12; targetH = h;
        }

        void slideTo(NavItem n, JComponent repaintTarget) {
            if (n == null) return;
            java.awt.Rectangle b = n.getBounds();
            targetY = b.y + 6;
            targetH = b.height - 12;
            if (Motion.REDUCED) { y = targetY; h = targetH; repaintTarget.repaint(); return; }
            if (handle != null) handle.cancel();
            final float startY = y, startH = h;
            handle = Animator.tween(Motion.MED, Easing.SPRING, t -> {
                y = startY + (targetY - startY) * (float) t;
                h = startH + (targetH - startH) * (float) t;
                repaintTarget.repaint();
            });
        }

        void paintInto(Graphics2D g, int totalHeight) {
            if (h <= 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                // outer halo
                Color glow = Theme.ACCENT_GLOW;
                for (int i = 0; i < 3; i++) {
                    g2.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(),
                        40 - i * 10));
                    g2.fillRoundRect(2 - i, (int) y - i, 4 + i * 2, (int) h + i * 2,
                        4, 4);
                }
                g2.setPaint(Gradient.bronzeSweep(4, (int) h));
                g2.fillRoundRect(0, (int) y, 4, (int) h, 4, 4);
            } finally {
                g2.dispose();
            }
        }
    }

    private static class NavItem extends JPanel {
        private final javax.swing.JLabel labelView;
        private final Badge badge;
        private final Supplier<Integer> countSupplier;
        private final IconCell icon;
        private boolean active;
        private boolean hover;

        NavItem(String key, String label, String iconName, Supplier<Integer> count) {
            this.countSupplier = count;
            setLayout(new BorderLayout());
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(Spacing.M, Spacing.L, Spacing.M, Spacing.L));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setAlignmentX(LEFT_ALIGNMENT);

            icon = new IconCell(iconName);

            labelView = new javax.swing.JLabel(label);
            labelView.setFont(Typography.BODY);
            labelView.setForeground(Theme.TEXT_SECONDARY);
            labelView.setBorder(BorderFactory.createEmptyBorder(0, Spacing.M, 0, 0));

            badge = new Badge("", Badge.Kind.ACCENT);
            badge.setVisible(false);

            JPanel left = new JPanel(new BorderLayout());
            left.setOpaque(false);
            left.add(icon, BorderLayout.WEST);
            left.add(labelView, BorderLayout.CENTER);

            add(left, BorderLayout.CENTER);
            add(badge, BorderLayout.EAST);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
            });
            refresh();
        }

        void refresh() {
            int c = 0;
            try { c = countSupplier.get(); } catch (RuntimeException ignored) { }
            if (c > 0) {
                badge.setText(String.valueOf(c));
                badge.setVisible(true);
            } else {
                badge.setVisible(false);
            }
        }

        void setActive(boolean a) {
            this.active = a;
            labelView.setForeground(a ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
            labelView.setFont(a ? Typography.BODY_BOLD : Typography.BODY);
            icon.setActive(a);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = Radius.MD;
                if (active) {
                    g2.setColor(Theme.BG_OVERLAY);
                    g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, arc, arc);
                } else if (hover) {
                    g2.setColor(new Color(Theme.BG_OVERLAY.getRed(), Theme.BG_OVERLAY.getGreen(),
                        Theme.BG_OVERLAY.getBlue(), 120));
                    g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, arc, arc);
                }
            } finally {
                g2.dispose();
            }
        }

        private static final class IconCell extends JComponent {
            private final String name;
            private boolean active;
            IconCell(String name) {
                this.name = name;
                Dimension d = new Dimension(22, 22);
                setPreferredSize(d);
                setMinimumSize(d);
                setMaximumSize(d);
            }
            void setActive(boolean a) { this.active = a; repaint(); }
            @Override
            protected void paintComponent(Graphics g) {
                Color c = active ? Theme.ACCENT : Theme.TEXT_SECONDARY;
                Iconography.paint((Graphics2D) g, name, 2, 2, 18f, c);
            }
        }
    }

    /** Suppressed — here so the rail can measure the expected width fence. */
    @SuppressWarnings("unused")
    private static int railFence(FontMetrics fm, Elevation.Tier t) { return 0; }
}
