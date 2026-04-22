package com.eventorganizer.ui.components;

import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
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
 * Left-rail navigation. Each item: 3px bronze rail when active, unread badge
 * when the corresponding section has pending state. Supply-chain layout:
 * Dashboard owns a Sidebar instance and calls {@link #refresh(UIController)}
 * whenever the underlying counts might have changed.
 */
public class Sidebar extends JPanel {

    private final Map<String, NavItem> items = new LinkedHashMap<>();
    private String active;

    public Sidebar(UIController controller, Consumer<String> onSelect) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);
        setBackground(Theme.BG_ELEVATED);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER_SUBTLE));
        setPreferredSize(new Dimension(220, 0));

        addItem("events",        "My Events",     () -> 0, onSelect);
        addItem("discover",      "Discover",      () -> controller.eventsInvitedTo().size(), onSelect);
        addItem("friends",       "Friends",       () -> controller.incomingReqs().size(), onSelect);
        addItem("notifications", "Notifications", controller::unreadCount, onSelect);
        addItem("reports",       "Reports",       () -> 0, onSelect);

        add(Box.createVerticalGlue());
        setActive("events");
    }

    private void addItem(String key, String label, Supplier<Integer> count, Consumer<String> onSelect) {
        NavItem item = new NavItem(key, label, count);
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                setActive(key);
                onSelect.accept(key);
            }
        });
        items.put(key, item);
        add(item);
    }

    public void setActive(String key) {
        active = key;
        for (Map.Entry<String, NavItem> e : items.entrySet()) {
            e.getValue().setActive(e.getKey().equals(key));
        }
        repaint();
    }

    public void refresh(UIController controller) {
        for (NavItem item : items.values()) item.refresh();
    }

    public String activeKey() { return active; }

    private static class NavItem extends JPanel {
        private final String key;
        private final JLabel labelView;
        private final Badge badge;
        private final Supplier<Integer> countSupplier;
        private boolean active;
        private boolean hover;

        NavItem(String key, String label, Supplier<Integer> count) {
            this.key = key;
            this.countSupplier = count;
            setLayout(new BorderLayout());
            setOpaque(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(Spacing.M, Spacing.L + 3, Spacing.M, Spacing.L));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            labelView = new JLabel(label);
            labelView.setFont(Theme.FONT_BODY);
            labelView.setForeground(Theme.TEXT_MUTED);

            badge = new Badge("", Badge.Kind.ACCENT);
            badge.setVisible(false);

            add(labelView, BorderLayout.WEST);
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
            labelView.setForeground(a ? Theme.TEXT_PRIMARY : Theme.TEXT_MUTED);
            labelView.setFont(a ? Theme.FONT_BODY_BOLD : Theme.FONT_BODY);
            repaint();
        }

        @Override
        public Color getBackground() {
            if (active) return Theme.BG_HOVER;
            if (hover)  return Theme.BG_HOVER;
            return Theme.BG_ELEVATED;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (active) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.ACCENT);
                    g2.fillRect(0, 0, 3, getHeight());
                } finally {
                    g2.dispose();
                }
            }
        }
    }
}
