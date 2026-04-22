package com.eventorganizer.ui.dialogs;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.enums.EventType;
import com.eventorganizer.services.EventService.CreateEventResult;
import com.eventorganizer.ui.components.FormField;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class CreateEventDialog {

    public static void show(Component parent, UIController controller, Runnable onCreated) {
        Window owner = parent == null ? null : javax.swing.SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(owner, "Create Event", JDialog.ModalityType.APPLICATION_MODAL);
        d.setLayout(new BorderLayout());

        JTextField name = new JTextField();
        JTextArea desc  = new JTextArea(4, 20);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(desc);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        SpinnerDateModel dateModel = new SpinnerDateModel(cal.getTime(), null, null, Calendar.MINUTE);
        JSpinner date = new JSpinner(dateModel);
        date.setEditor(new JSpinner.DateEditor(date, "yyyy-MM-dd HH:mm"));

        JTextField location = new JTextField();

        JRadioButton pub = new JRadioButton("Public", true);
        JRadioButton priv = new JRadioButton("Private");
        ButtonGroup grp = new ButtonGroup();
        grp.add(pub); grp.add(priv);
        JPanel typeRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typeRow.setOpaque(false);
        typeRow.add(pub); typeRow.add(priv);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Theme.BG_PRIMARY);
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 12, 20));
        form.add(new FormField("Name", name));
        form.add(new FormField("Description", descScroll));
        form.add(new FormField("Date/Time", date));
        form.add(new FormField("Location", location));
        form.add(new FormField("Type", typeRow));

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> d.dispose());
        JButton create = new JButton("Create");
        create.setMnemonic('R');
        create.addActionListener(e -> {
            try {
                Date dt = (Date) date.getValue();
                LocalDateTime when = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                EventType type = pub.isSelected() ? EventType.PUBLIC : EventType.PRIVATE;
                CreateEventResult result = controller.createEvent(
                    name.getText().trim(),
                    desc.getText(),
                    when,
                    location.getText().trim(),
                    type);
                d.dispose();
                if (result.getWarnings().isEmpty()) {
                    Toast.success(parent, "Event created.");
                } else {
                    Toast.warning(parent, result.getWarnings().get(0));
                }
                onCreated.run();
            } catch (AppException ex) {
                Toast.error(d.getContentPane(), ex.getMessage());
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.add(cancel);
        buttons.add(create);
        d.getRootPane().setDefaultButton(create);

        d.add(form, BorderLayout.CENTER);
        d.add(buttons, BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(parent);
        d.setVisible(true);
    }
}
