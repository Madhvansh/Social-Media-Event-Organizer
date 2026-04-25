package com.eventorganizer.ui.components;

import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

/**
 * Label + input + reserved error slot. Error state turns the input border red
 * and fills the error slot with the error text.
 *
 * <p>Uses Typography tokens for the label (small, tracked caps) and the error
 * line (small, danger-colored).
 */
public class FormField extends JPanel {

    private final JLabel errorLabel;
    private final JComponent input;
    private final Border normalBorder;
    private final Border errorBorder;

    public FormField(String labelText, JComponent input) {
        this.input = input;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText == null ? "" : labelText);
        label.setForeground(Theme.TEXT_SECONDARY);
        label.setFont(Typography.LABEL);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setLabelFor(input);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, Spacing.XS, 0));

        normalBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(Spacing.S, Spacing.M, Spacing.S, Spacing.M));
        errorBorder = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.DANGER, 1, true),
            BorderFactory.createEmptyBorder(Spacing.S, Spacing.M, Spacing.S, Spacing.M));

        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setBorder(normalBorder);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, input.getPreferredSize().height + (Spacing.S * 2)));

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setFont(Typography.SMALL);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        errorLabel.setBorder(BorderFactory.createEmptyBorder(Spacing.XS, 0, 0, 0));

        add(label);
        add(input);
        add(errorLabel);
        setBorder(BorderFactory.createEmptyBorder(0, 0, Spacing.M, 0));
    }

    public void setError(String msg) {
        boolean hasError = msg != null && !msg.isEmpty();
        errorLabel.setText(hasError ? msg : " ");
        input.setBorder(hasError ? errorBorder : normalBorder);
        input.repaint();
    }

    public void clearError() { setError(null); }

    @Override
    public Color getBackground() { return new Color(0, 0, 0, 0); }
}
