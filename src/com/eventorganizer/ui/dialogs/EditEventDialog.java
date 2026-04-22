package com.eventorganizer.ui.dialogs;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.Event;
import com.eventorganizer.ui.components.FormField;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class EditEventDialog {
    public static void show(Component parent, UIController controller, Event event, Runnable onSaved) {
        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(owner, "Edit Event", JDialog.ModalityType.APPLICATION_MODAL);
        d.setLayout(new BorderLayout());

        JTextField name = new JTextField(event.getName());
        JTextArea desc  = new JTextArea(event.getDescription(), 4, 20);
        desc.setLineWrap(true); desc.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(desc);
        JTextField location = new JTextField(event.getLocation());

        Date current = Timestamp.valueOf(event.getDateTime());
        SpinnerDateModel dateModel = new SpinnerDateModel(current, null, null, Calendar.MINUTE);
        JSpinner date = new JSpinner(dateModel);
        date.setEditor(new JSpinner.DateEditor(date, "yyyy-MM-dd HH:mm"));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Theme.BG_PRIMARY);
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
        form.add(new FormField("Name", name));
        form.add(new FormField("Description", descScroll));
        form.add(new FormField("Date/Time", date));
        form.add(new FormField("Location", location));

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> d.dispose());
        JButton save = new JButton("Save");
        save.setMnemonic('S');
        save.addActionListener(e -> {
            try {
                Date dt = (Date) date.getValue();
                LocalDateTime when = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                controller.editEvent(event.getEventId(),
                    name.getText().trim(), desc.getText(), when, location.getText().trim());
                d.dispose();
                Toast.success(parent, "Event updated.");
                onSaved.run();
            } catch (AppException ex) {
                Toast.error(d.getContentPane(), ex.getMessage());
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.add(cancel);
        buttons.add(save);
        d.getRootPane().setDefaultButton(save);

        d.add(form, BorderLayout.CENTER);
        d.add(buttons, BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }
}
