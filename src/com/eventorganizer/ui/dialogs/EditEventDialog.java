package com.eventorganizer.ui.dialogs;

import com.eventorganizer.models.Event;
import com.eventorganizer.ui.components.AsyncUI;
import com.eventorganizer.ui.components.FormField;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * Edit event dialog. Two-column layout matching {@link CreateEventDialog}.
 * Includes a "changes will notify all invitees" tip at the bottom of the form.
 */
public class EditEventDialog extends AbstractAppDialog {

    public static void show(Component parent, UIController controller, Event event, Runnable onSaved) {
        new EditEventDialog(parent, controller, event, onSaved).showCentered(parent);
    }

    private EditEventDialog(Component parent, UIController controller, Event event, Runnable onSaved) {
        super(parent, "Edit event", 720, 600);

        AuroraTextField name = new AuroraTextField();
        name.setText(event.getName() == null ? "" : event.getName());

        JTextArea desc  = new JTextArea(event.getDescription(), 6, 28);
        desc.setLineWrap(true); desc.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(desc);

        AuroraTextField location = new AuroraTextField();
        location.setText(event.getLocation() == null ? "" : event.getLocation());

        Date current = Timestamp.valueOf(event.getDateTime());
        SpinnerDateModel dateModel = new SpinnerDateModel(current, null, null, Calendar.MINUTE);
        JSpinner date = new JSpinner(dateModel);
        date.setEditor(new JSpinner.DateEditor(date, "yyyy-MM-dd HH:mm"));

        FormField nameFF = new FormField("EVENT NAME", name);
        FormField descFF = new FormField("DESCRIPTION", descScroll);
        FormField dateFF = new FormField("DATE & TIME", date);
        FormField locFF  = new FormField("LOCATION", location);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(nameFF);
        left.add(descFF);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(dateFF);
        right.add(locFF);

        JLabel notice = new JLabel("Saving will notify all invitees of any changes.");
        notice.setFont(Typography.SMALL);
        notice.setForeground(Theme.WARNING);
        notice.setAlignmentX(Component.LEFT_ALIGNMENT);
        notice.setBorder(BorderFactory.createEmptyBorder(Spacing.S, 0, 0, 0));
        right.add(notice);

        JPanel form = new JPanel(new GridLayout(1, 2, Spacing.XL, 0));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.L, Spacing.XL));
        form.add(left);
        form.add(right);

        AuroraButton cancel = new AuroraButton("Cancel", AuroraButton.Variant.GHOST);
        cancel.addActionListener(e -> dispose());
        AuroraButton save = new AuroraButton("Save changes", AuroraButton.Variant.DEFAULT);
        save.setMnemonic('S');
        save.addActionListener(e -> {
            Date dt = (Date) date.getValue();
            LocalDateTime when = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            String nameVal = name.getText().trim();
            String descVal = desc.getText();
            String locVal  = location.getText().trim();
            AsyncUI.run(save,
                () -> { controller.editEvent(event.getEventId(), nameVal, descVal, when, locVal); },
                () -> {
                    dispose();
                    Toast.success(parent, "Event updated.");
                    onSaved.run();
                },
                ex -> Toast.error(getContentPane(), ex.getMessage()));
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Spacing.S, Spacing.S));
        buttons.setOpaque(false);
        buttons.setBorder(BorderFactory.createEmptyBorder(0, Spacing.XL, Spacing.L, Spacing.XL));
        buttons.add(cancel);
        buttons.add(save);
        getRootPane().setDefaultButton(save);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }
}
