package com.eventorganizer.ui.components;

import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

public class FormField extends JPanel {
    private final JLabel errorLabel;

    public FormField(String labelText, JComponent input) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setForeground(Theme.TEXT_MUTED);
        label.setFont(Theme.FONT_SMALL);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setLabelFor(input);

        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, input.getPreferredSize().height + 16));

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setFont(Theme.FONT_SMALL);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(label);
        add(input);
        add(errorLabel);
        setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
    }

    public void setError(String msg) {
        errorLabel.setText(msg == null || msg.isEmpty() ? " " : msg);
    }

    public void clearError() { setError(null); }

    @Override
    public Color getBackground() { return new Color(0, 0, 0, 0); }
}
