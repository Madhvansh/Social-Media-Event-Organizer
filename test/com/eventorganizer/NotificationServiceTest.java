package com.eventorganizer;

import com.eventorganizer.models.EventUpdateNotification;
import com.eventorganizer.models.InvitationNotification;
import com.eventorganizer.models.Notification;
import com.eventorganizer.models.User;
import com.eventorganizer.services.NotificationService;
import com.eventorganizer.services.UserService;
import com.eventorganizer.store.TestHooks;
import com.eventorganizer.utils.IdGenerator;
import com.eventorganizer.utils.Limits;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationServiceTest {

    private UserService users;
    private NotificationService notifications;
    private User alice;

    @BeforeEach
    void reset() {
        TestHooks.reset();
        users = new UserService();
        notifications = new NotificationService();
        alice = users.register("alice", "a@example.com", "alice123");
    }

    @Test
    @DisplayName("push delivers a notification")
    void pushDelivers() {
        notifications.push(alice, new InvitationNotification(
            IdGenerator.nextNotificationId(), alice.getUserId(),
            "You have been invited.", "evt-1"));
        assertEquals(1, alice.getNotifications().size());
    }

    @Test
    @DisplayName("markAllAsRead clears unread flag on every notification")
    void markAllRead() {
        for (int i = 0; i < 3; i++) {
            notifications.push(alice, new InvitationNotification(
                IdGenerator.nextNotificationId(), alice.getUserId(),
                "msg", "evt-" + i));
        }
        notifications.markAllAsRead(alice);
        for (Notification n : alice.getNotifications()) {
            assertTrue(n.isRead());
        }
    }

    @Test
    @DisplayName("notification cap evicts the oldest read entry first")
    void notificationCapEviction() {
        int cap = Limits.NOTIFICATIONS_PER_USER_MAX;
        // First push is marked read immediately, so it should be evicted first on overflow.
        InvitationNotification firstRead = new InvitationNotification(
            IdGenerator.nextNotificationId(), alice.getUserId(), "read", "evt-0");
        firstRead.markAsRead();
        alice.addNotification(firstRead);
        // Fill to cap with unread (cap-1 more).
        for (int i = 1; i < cap; i++) {
            alice.addNotification(new InvitationNotification(
                IdGenerator.nextNotificationId(), alice.getUserId(), "u" + i, "evt-" + i));
        }
        assertEquals(cap, alice.getNotifications().size());
        // One more — should evict the single read entry.
        alice.addNotification(new InvitationNotification(
            IdGenerator.nextNotificationId(), alice.getUserId(), "last", "evt-last"));
        assertEquals(cap, alice.getNotifications().size());
        assertFalse(alice.getNotifications().contains(firstRead));
    }

    @Test
    @DisplayName("pushCoalesced replaces a recent same-event update")
    void coalesceRecent() {
        EventUpdateNotification first = new EventUpdateNotification(
            IdGenerator.nextNotificationId(), alice.getUserId(),
            "Edited - round 1", "evt-1");
        notifications.pushCoalesced(alice, first);
        EventUpdateNotification second = new EventUpdateNotification(
            IdGenerator.nextNotificationId(), alice.getUserId(),
            "Edited - round 2", "evt-1");
        notifications.pushCoalesced(alice, second);
        assertEquals(1, alice.getNotifications().size());
    }

    @Test
    @DisplayName("pushCoalesced keeps separate eventIds separate")
    void coalesceDoesNotMergeDifferentEvents() {
        notifications.pushCoalesced(alice, new EventUpdateNotification(
            IdGenerator.nextNotificationId(), alice.getUserId(), "A", "evt-1"));
        notifications.pushCoalesced(alice, new EventUpdateNotification(
            IdGenerator.nextNotificationId(), alice.getUserId(), "B", "evt-2"));
        assertEquals(2, alice.getNotifications().size());
    }

    @Test
    @DisplayName("getUnreadForCurrentUser filters out read notifications")
    void unreadFilter() {
        users.login("alice", "alice123");
        InvitationNotification a = new InvitationNotification(
            IdGenerator.nextNotificationId(), alice.getUserId(), "a", "evt-1");
        a.markAsRead();
        alice.addNotification(a);
        alice.addNotification(new InvitationNotification(
            IdGenerator.nextNotificationId(), alice.getUserId(), "b", "evt-2"));
        assertEquals(1, notifications.getUnreadForCurrentUser().size());
    }
}
