package com.eventorganizer.ui.dialogs;

import com.eventorganizer.exceptions.AppException;
import com.eventorganizer.models.enums.EventType;
import com.eventorganizer.services.EventService.CreateEventResult;
import com.eventorganizer.ui.components.AsyncUI;
import com.eventorganizer.ui.components.FormField;
import com.eventorganizer.ui.components.Toast;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.theme.Spacing;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class CreateEventDialog extends AbstractAppDialog {

    public static void show(Component parent, UIController controller, Runnable onCreated) {
        new CreateEventDialog(parent, controller, onCreated).showCentered(parent);
    }

    private CreateEventDialog(Component parent, UIController controller, Runnable onCreated) {
        super(parent, "Create Event", 480, 560);

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
        JPanel typeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, Spacing.S, 0));
        typeRow.setOpaque(false);
        typeRow.add(pub); typeRow.add(priv);

        FormField nameFF = new FormField("Name", name);
        FormField descFF = new FormField("Description", descScroll);
        FormField dateFF = new FormField("Date & time", date);
        FormField locFF  = new FormField("Location", location);
        FormField typeFF = new FormField("Type", typeRow);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Theme.BG_PRIMARY);
        form.setBorder(BorderFactory.createEmptyBorder(Spacing.XL, Spacing.XL, Spacing.L, Spacing.XL));
        form.add(nameFF);
        form.add(descFF);
        form.add(dateFF);
        form.add(locFF);
        form.add(typeFF);

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JButton create = new JButton("Create");
        create.setMnemonic('R');
        create.putClientProperty("JButton.buttonType", "default");
        create.addActionListener(e -> {
            nameFF.clearError();
            locFF.clearError();
            dateFF.clearError();
            Date dt = (Date) date.getValue();
            LocalDateTime when = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            EventType type = pub.isSelected() ? EventType.PUBLIC : EventType.PRIVATE;
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
