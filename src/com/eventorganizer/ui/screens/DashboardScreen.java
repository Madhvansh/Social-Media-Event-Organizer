package com.eventorganizer.ui.screens;

import com.eventorganizer.ui.components.Sidebar;
import com.eventorganizer.ui.components.StatusBar;
import com.eventorganizer.ui.components.SurfacePanel;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.screens.panels.DiscoverEventsPanel;
import com.eventorganizer.ui.screens.panels.FriendsPanel;
import com.eventorganizer.ui.screens.panels.MyEventsPanel;
import com.eventorganizer.ui.screens.panels.NotificationsPanel;
import com.eventorganizer.ui.screens.panels.ProfilePanel;
import com.eventorganizer.ui.screens.panels.ReportsPanel;
import com.eventorganizer.ui.theme.Theme;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;

public class DashboardScreen extends SurfacePanel {

    private final UIController controller;
    @SuppressWarnings("unused")
    private final Runnable onLogout;

    private final CardLayout center = new CardLayout();
    private final JPanel centerPanel = new JPanel(center);

    private final MyEventsPanel      events;
    private final DiscoverEventsPanel discover;
    private final FriendsPanel        friends;
    private final NotificationsPanel  notifications;
    private final ReportsPanel        reports;
    private final ProfilePanel        profile;

    private final StatusBar statusBar;
    private final Sidebar sidebar;

    public DashboardScreen(UIController controller, Runnable onLogout) {
        super(new BorderLayout(), Theme.BG_PRIMARY, true);
        this.controller = controller;
        this.onLogout = onLogout;

        events        = new MyEventsPanel(controller, this::refreshAll);
        discover      = new DiscoverEventsPanel(controller, this::refreshAll);
        friends       = new FriendsPanel(controller, this::refreshAll);
        notifications = new NotificationsPanel(controller, this::refreshAll);
        reports       = new ReportsPanel(controller);
        profile       = new ProfilePanel(controller);

        centerPanel.setOpaque(false);
        centerPanel.add(events,        "events");
        centerPanel.add(discover,      "discover");
        centerPanel.add(friends,       "friends");
        centerPanel.add(notifications, "notifications");
        centerPanel.add(reports,       "reports");
        centerPanel.add(profile,       "profile");

        statusBar = new StatusBar(controller,
            () -> show("profile"),
            () -> { controller.logout(); onLogout.run(); });

        sidebar = new Sidebar(controller, this::show);

        add(statusBar,  BorderLayout.NORTH);
        add(sidebar,    BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void show(String key) {
        center.show(centerPanel, key);
        switch (key) {
            case "events":        events.refresh(); break;
            case "discover":      discover.refresh(); break;
            case "friends":       friends.refresh(); break;
            case "notifications": notifications.refresh(); break;
            case "reports":       reports.refresh(); break;
            case "profile":       profile.refresh(); break;
            default: break;
        }
        statusBar.refresh(controller);
        sidebar.refresh(controller);
    }

    public void onEnter() {
        events.refresh();
        discover.refresh();
        friends.refresh();
        notifications.refresh();
        reports.refresh();
        profile.refresh();
        statusBar.refresh(controller);
        sidebar.refresh(controller);
        show("events");
    }

    private void refreshAll() {
        events.refresh();
        discover.refresh();
        friends.refresh();
        notifications.refresh();
        reports.refresh();
        statusBar.refresh(controller);
        sidebar.refresh(controller);
    }
}
