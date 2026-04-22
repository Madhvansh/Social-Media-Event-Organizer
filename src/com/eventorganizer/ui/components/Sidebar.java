package com.eventorganizer.ui.components;

import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public class Sidebar extends JPanel {

    public Sidebar(Consumer<String> onSelect) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.BG_ELEVATED);
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));
        setPreferredSize(new Dimension(200, 0));

        ButtonGroup group = new ButtonGroup();
        String[][] items = {
            {"events",       "My Events"},
            {"discover",     "Discover"},
            {"friends",      "Friends"},
            {"notifications","Notifications"},
            {"reports",      "Reports"}
        };
        boolean first = true;
        for (String[] item : items) {
            JToggleButton btn = navButton(item[1]);
            btn.addActionListener(clickListener(onSelect, item[0]));
            group.add(btn);
            if (first) { btn.setSelected(true); first = false; }
            add(btn);
            add(Box.createVerticalStrut(4));
        }
        add(Box.createVerticalGlue());
    }

    private static ActionListener clickListener(Consumer<String> onSelect, String key) {
        return e -> onSelect.accept(key);
    }

    private JToggleButton navButton(String label) {
        JToggleButton b = new JToggleButton(label);
        b.setFont(Theme.FONT_BODY);
        b.setForeground(Theme.TEXT_PRIMARY);
        b.setBackground(Theme.BG_ELEVATED);
        b.setFocusPainted(true);
        b.setBorderPainted(false);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setHorizontalAlignment(JToggleButton.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return b;
    }
}
