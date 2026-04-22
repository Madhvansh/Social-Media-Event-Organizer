package com.eventorganizer.ui;

import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.screens.AuthScreen;
import com.eventorganizer.ui.screens.DashboardScreen;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.CardLayout;
import java.awt.Dimension;

public class App {

    private static final String CARD_AUTH = "auth";
    private static final String CARD_DASH = "dash";

    private JFrame frame;
    private CardLayout layout;
    private JPanel root;
    private UIController controller;
    private DashboardScreen dashboard;

    public void start() {
        SwingUtilities.invokeLater(this::buildAndShow);
    }

    private void buildAndShow() {
        controller = new UIController();

        frame = new JFrame("Event Organizer");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(1200, 800));
        frame.getContentPane().setBackground(Theme.BG_PRIMARY);

        layout = new CardLayout();
        root = new JPanel(layout);
        root.setBackground(Theme.BG_PRIMARY);

        AuthScreen auth = new AuthScreen(controller, v -> showDashboard());
        root.add(auth, CARD_AUTH);

        dashboard = new DashboardScreen(controller, this::showAuth);
        root.add(dashboard, CARD_DASH);

        frame.setContentPane(root);
        frame.pack();
        frame.setLocationRelativeTo(null);
        layout.show(root, CARD_AUTH);
        frame.setVisible(true);
    }

    private void showDashboard() {
        dashboard.onEnter();
        layout.show(root, CARD_DASH);
    }

    private void showAuth() {
        layout.show(root, CARD_AUTH);
    }
}
