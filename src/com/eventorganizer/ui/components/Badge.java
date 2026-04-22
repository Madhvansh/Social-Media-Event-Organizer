package com.eventorganizer.ui.components;

import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import java.awt.Color;

public class Badge extends JLabel {
    public Badge(String text, Color accent) {
        super(text);
        setOpaque(true);
        setBackground(accent.darker());
        setForeground(Theme.TEXT_PRIMARY);
        setFont(Theme.FONT_SMALL);
        setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
    }
}
