package com.eventorganizer.ui.components;

import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * 24 px bottom status strip: "connected · in-memory · username · v1.0" in
 * mono-tertiary text. Low-key but adds a professional finish to the shell.
 */
public final class FootStrip extends JPanel {

    private final JLabel text = new JLabel();

    public FootStrip() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Theme.BG_CANVAS);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_SUBTLE),
            BorderFactory.createEmptyBorder(4, Spacing.L, 4, Spacing.L)));
        setPreferredSize(new Dimension(0, 24));

        text.setFont(Typography.MONO);
        text.setForeground(Theme.TEXT_TERTIARY);
        add(text, BorderLayout.WEST);
    }

    public void set(String... segments) {
        if (segments == null || segments.length == 0) {
            text.setText("");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            if (i > 0) sb.append("  ·  ");
            sb.append(segments[i]);
        }
        text.setText(sb.toString());
    }
}
