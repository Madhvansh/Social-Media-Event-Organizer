package com.eventorganizer.ui.dialogs;

import com.eventorganizer.models.enums.EventType;
import com.eventorganizer.services.EventService.CreateEventResult;
import com.eventorganizer.ui.components.AsyncUI;
import com.eventorganizer.ui.components.FormField;
import com.eventorganizer.ui.components.SegmentedControl;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.laf.AuroraButton;
import com.eventorganizer.ui.laf.AuroraTextField;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;
import com.eventorganizer.ui.theme.Typography;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerDateModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * Create event dialog — two-column form. Left: name + description. Right:
 * date/time spinner + Public/Private SegmentedControl + location.
 */
public class CreateEventDialog extends AbstractAppDialog {

    public static void show(Component parent, UIController controller, Runnable onCreated) {
        new CreateEventDialog(parent, controller, onCreated).showCentered(parent);
    }

    private CreateEventDialog(Component parent, UIController controller, Runnable onCreated) {
        super(parent, "Create event", 720, 600);

        AuroraTextField name = new AuroraTextField("Event name");
        JTextArea desc  = new JTextArea(6, 28);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(desc);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        SpinnerDateModel dateModel = new SpinnerDateModel(cal.getTime(), null, null, Calendar.MINUTE);
        JSpinner date = new JSpinner(dateModel);
        date.setEditor(new JSpinner.DateEditor(date, "yyyy-MM-dd HH:mm"));

        AuroraTextField location = new AuroraTextField("Location");

        SegmentedControl typeToggle = new SegmentedControl();
        typeToggle.addSegment("Public");
        typeToggle.addSegment("Private");

        FormField nameFF = new FormField("EVENT NAME", name);
        FormField descFF = new FormField("DESCRIPTION", descScroll);
        FormField dateFF = new FormField("DATE & TIME", date);
        FormField locFF  = new FormField("LOCATION", location);

        // Left column: name + description
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(nameFF);
        left.add(descFF);

        // Right column: date + type + location + tip
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(dateFF);

        JPanel typeSlot = new JPanel(new BorderLayout());
        typeSlot.setOpaque(false);
        typeSlot.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel typeLabel = new JLabel("EVENT TYPE");
        typeLabel.setFont(Typography.LABEL);
        typeLabel.setForeground(Theme.TEXT_SECONDARY);
        typeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, Spacing.XS, 0));
        typeSlot.add(typeLabel, BorderLayout.NORTH);
        JPanel toggleWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        toggleWrap.setOpaque(false);
        toggleWrap.add(typeToggle);
        typeSlot.add(toggleWrap, BorderLayout.CENTER);
        typeSlot.setBorder(BorderFactory.createEmptyBorder(0, 0, Spacing.M, 0));
        right.add(typeSlot);

        right.add(locFF);

        JLabel tip = new JLabel("Public events let any user RSVP. Private events are friends-only.");
        tip.setFont(Typography.SMALL);
        tip.setForeground(Theme.TEXT_TERTIARY);
        tip.setAlignmentX(Component.LEFT_ALIGNMENT);
        tip.setBorder(BorderFactory.createEmptyBorder(Spacing.XS, 0, 0, 0));
        right.add(tip);

        JPanel form = new JPanel(new GridLayout(1, 2, Spacing.XL, 0));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.L, Spacing.XL));
        form.add(left);
        form.add(right);

        AuroraButton cancel = new AuroraButton("Cancel", AuroraButton.Variant.GHOST);
        cancel.addActionListener(e -> dispose());
        AuroraButton create = new AuroraButton("Create event", AuroraButton.Variant.DEFAULT);
        create.setMnemonic('R');
        create.addActionListener(e -> {
            nameFF.clearError();
            locFF.clearError();
            dateFF.clearError();
            Date dt = (Date) date.getValue();
            LocalDateTime when = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            EventType type = typeToggle.getSelectedIndex() == 0 ? EventType.PUBLIC : EventType.PRIVATE;
            String nameVal = name.getText().trim();
            String descVal = desc.getText();
            String locVal  = location.getText().trim();
            AsyncUI.run(create,
                () -> controller.createEvent(nameVal, descVal, when, locVal, type),
                (CreateEventResult result) -> {
                    dispose();
                    if (result.getWarnings().isEmpty()) {
                        Toast.success(parent, "Event created.");
                    } else {
                        Toast.warning(parent, result.getWarnings().get(0));
                    }
                    onCreated.run();
                },
                ex -> Toast.error(getContentPane(), ex.getMessage()));
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, Spacing.S));
        buttons.setOpaque(false);
        buttons.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.L, Spacing.XL));
        buttons.add(cancel);
        buttons.add(create);
        getRootPane().setDefaultButton(create);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }
}
