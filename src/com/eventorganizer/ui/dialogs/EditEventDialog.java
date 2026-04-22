package com.eventorganizer.ui.dialogs;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.Event;
import com.eventorganizer.ui.components.AsyncUI;
import com.eventorganizer.ui.components.FormField;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class EditEventDialog extends AbstractAppDialog {

    public static void show(Component parent, UIController controller, Event event, Runnable onSaved) {
        new EditEventDialog(parent, controller, event, onSaved).showCentered(parent);
    }

    private EditEventDialog(Component parent, UIController controller, Event event, Runnable onSaved) {
        super(parent, "Edit Event", 480, 560);

        JTextField name = new JTextField(event.getName());
        JTextArea desc  = new JTextArea(event.getDescription(), 4, 20);
        desc.setLineWrap(true); desc.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(desc);
        JTextField location = new JTextField(event.getLocation());

        Date current = Timestamp.valueOf(event.getDateTime());
        SpinnerDateModel dateModel = new SpinnerDateModel(current, null, null, Calendar.MINUTE);
        JSpinner date = new JSpinner(dateModel);
        date.setEditor(new JSpinner.DateEditor(date, "yyyy-MM-dd HH:mm"));

        FormField nameFF = new FormField("Name", name);
        FormField descFF = new FormField("Description", descScroll);
        FormField dateFF = new FormField("Date & time", date);
        FormField locFF  = new FormField("Location", location);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Theme.BG_PRIMARY);
        form.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.L, Spacing.XL));
        form.add(nameFF);
        form.add(descFF);
        form.add(dateFF);
        form.add(locFF);

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton save = new JButton("Save");
        save.setMnemonic('S');
        save.putClientProperty("JButton.buttonType", "default");
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
