package com.eventorganizer.ui.screens;

import com.eventorganizer.ui.components.FootStrip;
import com.eventorganizer.ui.components.Sidebar;
import com.eventorganizer.ui.components.StatusBar;
import com.eventorganizer.ui.controllers.UIController;
import com.eventorganizer.ui.fx.PanelAnimator;
import com.eventorganizer.ui.screens.panels.DiscoverEventsPanel;
import com.eventorganizer.ui.screens.panels.FriendsPanel;
import com.eventorganizer.ui.screens.panels.MyEventsPanel;
import com.eventorganizer.ui.screens.panels.NotificationsPanel;
import com.eventorganizer.ui.screens.panels.ProfilePanel;
import com.eventorganizer.ui.screens.panels.ReportsPanel;

import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;

/**
 * Dashboard shell: {@link StatusBar} (Mast) at top, {@link Sidebar} (NavRail)
 * at left, panels in the center wrapped in {@link JLayer} for fade transitions,
 * {@link FootStrip} at the bottom.
 */
public class DashboardScreen extends JPanel {

    private final UIController controller;
    @SuppressWarnings("unused")
    private final Runnable onLogout;

    private final CardLayout center = new CardLayout();
    private final JPanel centerPanel = new JPanel(center);
    private final PanelAnimator centerFader = new PanelAnimator();
    private final JLayer<JComponent> centerLayer;

    private final MyEventsPanel      events;
    private final DiscoverEventsPanel discover;
    private final FriendsPanel        friends;
    private final NotificationsPanel  notifications;
    private final ReportsPanel        reports;
    private final ProfilePanel        profile;

    private final StatusBar mast;
    private final Sidebar navRail;
    private final FootStrip foot;

    public DashboardScreen(UIController controller, Runnable onLogout) {
        super(new BorderLayout());
        setOpaque(false);
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

        centerLayer = new JLayer<>((JComponent) centerPanel, centerFader);

        mast = new StatusBar(controller,
            () -> show("profile"),
            () -> { controller.logout(); onLogout.run(); });

        navRail = new Sidebar(controller, this::show);

        foot = new FootStrip();

        add(mast,       BorderLayout.NORTH);
        add(navRail,    BorderLayout.WEST);
        add(centerLayer, BorderLayout.CENTER);
        add(foot,       BorderLayout.SOUTH);
    }

    public void show(String key) {
        center.show(centerPanel, key);
        navRail.setActive(key);
        switch (key) {
            case "events":        events.refresh(); break;
            case "discover":      discover.refresh(); break;
            case "friends":       friends.refresh(); break;
            case "notifications": notifications.refresh(); break;
            case "reports":       reports.refresh(); break;
            case "profile":       profile.refresh(); break;
            default: break;
        }
        mast.refresh(controller);
        navRail.refresh(controller);
        refreshFoot();
        centerFader.trigger();
    }

    public void onEnter() {
        events.refresh();
        discover.refresh();
        friends.refresh();
        notifications.refresh();
        reports.refresh();
        profile.refresh();
        mast.refresh(controller);
        navRail.refresh(controller);
        refreshFoot();
        show("events");
    }

    private void refreshAll() {
        events.refresh();
        discover.refresh();
        friends.refresh();
        notifications.refresh();
        reports.refresh();
        mast.refresh(controller);
        navRail.refresh(controller);
        refreshFoot();
    }

    private void refreshFoot() {
        var u = controller.currentUser();
        foot.set(
            "connected",
            "in-memory",
            u == null ? "—" : u.getUsername(),
            "v1.0");
    }
}
