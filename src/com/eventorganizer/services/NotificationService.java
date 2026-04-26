package com.eventorganizer.services;

import com.eventorganizer.exceptions.UnauthorizedException;
import com.eventorganizer.models.EventUpdateNotification;
import com.eventorganizer.models.Notification;
import com.eventorganizer.models.User;
import com.eventorganizer.store.DataStore;
import com.eventorganizer.utils.Limits;
import com.eventorganizer.utils.LogConfig;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificationService {

    private static final Logger LOG = LogConfig.forClass(NotificationService.class);

    public void push(User recipient, Notification n) {
        if (recipient == null || n == null) return;
        try {
            if (!recipient.addNotification(n)) {
                LOG.log(Level.WARNING,
                    "Notification queue full for user " + recipient.getUserId()
                        + " - oldest unread retained, new notification dropped.");
            }
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING,
                "Notification dispatch failed for user " + recipient.getUserId(), e);
        }
    }

    /**
     * Event-update dispatch that coalesces rapid edits: if the recipient already has
     * an EventUpdateNotification for the same eventId whose timestamp is within the
     * {@link Limits#EDIT_COALESCE_SECONDS} window, the old entry is removed so the
     * newer message replaces it (same position-at-end, fresh timestamp).
     */
    public void pushCoalesced(User recipient, EventUpdateNotification n) {
        if (recipient == null || n == null) return;
        try {
            LocalDateTime cutoff = LocalDateTime.now(DataStore.INSTANCE.getClock())
                .minusSeconds(Limits.EDIT_COALESCE_SECONDS);
            recipient.removeNotificationsMatching(existing ->
                existing instanceof EventUpdateNotification
                && ((EventUpdateNotification) existing).getEventId().equals(n.getEventId())
                && existing.getTimestamp().isAfter(cutoff));
            if (!recipient.addNotification(n)) {
                LOG.log(Level.WARNING,
                    "Notification queue full for user " + recipient.getUserId()
                        + " - oldest unread retained, new notification dropped.");
            }
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING,
                "Notification dispatch failed for user " + recipient.getUserId(), e);
        }
    }

    public List<Notification> getUnreadForCurrentUser() {
        User u = currentUser();
        List<Notification> list = new ArrayList<>(u.getUnreadNotifications());
        list.sort(Comparator.comparing(Notification::getTimestamp).reversed());
        return list;
    }

    public List<Notification> getAllForCurrentUser() {
        User u = currentUser();
        List<Notification> list = new ArrayList<>(u.getNotifications());
        list.sort(Comparator.comparing(Notification::getTimestamp).reversed());
        return list;
    }

    public void markAllRead() {
        User u = currentUser();
        u.markAllNotificationsAsRead();
    }

    public void markAllAsRead(User user) {
        if (user == null) throw new UnauthorizedException("User is required.");
        user.markAllNotificationsAsRead();
    }

    public int countUnread() {
        User u = DataStore.INSTANCE.getCurrentUser();
        return u == null ? 0 : u.getUnreadNotifications().size();
    }

    public List<Notification> getEmpty() {
        return Collections.emptyList();
    }

    private User currentUser() {
        User u = DataStore.INSTANCE.getCurrentUser();
        if (u == null) throw new UnauthorizedException("Login required.");
        return u;
    }
}
