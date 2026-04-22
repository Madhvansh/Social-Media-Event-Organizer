package com.eventorganizer;

import com.eventorganizer.exceptions.DuplicateInvitationException;
import com.eventorganizer.exceptions.InvalidOperationException;
import com.eventorganizer.models.Event;
import com.eventorganizer.models.User;
import com.eventorganizer.models.dto.BatchInviteResult;
import com.eventorganizer.models.enums.EventType;
import com.eventorganizer.services.EventService;
import com.eventorganizer.services.InvitationService;
import com.eventorganizer.services.UserService;
import com.eventorganizer.store.TestHooks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvitationServiceTest {

    private UserService users;
    private EventService events;
    private InvitationService invs;
    private User alice;
    private User bob;
    private User carol;

    @BeforeEach
    void reset() {
        TestHooks.reset();
        users = new UserService();
        events = new EventService();
        invs = new InvitationService();
        alice = users.register("alice", "a@example.com", "alice123");
        bob   = users.register("bob",   "b@example.com", "bob12345");
        carol = users.register("carol", "c@example.com", "carol123");
    }

    @Test
    @DisplayName("private event: only friends can be invited (polymorphic canInvite)")
    void privateEventRequiresFriendship() {
        users.login("alice", "alice123");
        Event ev = events.createEvent("Secret", "friends only",
            LocalDateTime.now().plusDays(2), "Alice's Home", EventType.PRIVATE);
        // bob is not a friend — private rule blocks.
        assertThrows(InvalidOperationException.class,
            () -> invs.inviteFriend(ev.getEventId(), "bob"));
        alice.addFriend(bob);
        bob.addFriend(alice);
        assertEquals(ev.getEventId(), invs.inviteFriend(ev.getEventId(), "bob").getEventId());
    }

    @Test
    @DisplayName("public event: anyone can be invited")
    void publicEventOpen() {
        users.login("alice", "alice123");
        Event ev = events.createEvent("Meetup", "", LocalDateTime.now().plusDays(2),
            "Park", EventType.PUBLIC);
        assertEquals(ev.getEventId(), invs.inviteFriend(ev.getEventId(), "bob").getEventId());
    }

    @Test
    @DisplayName("duplicate invitation rejected")
    void duplicateRejected() {
        users.login("alice", "alice123");
        Event ev = events.createEvent("Meetup", "", LocalDateTime.now().plusDays(2),
            "Park", EventType.PUBLIC);
        invs.inviteFriend(ev.getEventId(), "bob");
        assertThrows(DuplicateInvitationException.class,
            () -> invs.inviteFriend(ev.getEventId(), "bob"));
    }

    @Test
    @DisplayName("self-invite blocked")
    void selfInviteBlocked() {
        users.login("alice", "alice123");
        Event ev = events.createEvent("Meetup", "", LocalDateTime.now().plusDays(2),
            "Park", EventType.PUBLIC);
        assertThrows(InvalidOperationException.class,
            () -> invs.inviteFriend(ev.getEventId(), "alice"));
    }

    @Test
    @DisplayName("revoke a pending invitation")
    void revokePending() {
        users.login("alice", "alice123");
        Event ev = events.createEvent("Meetup", "", LocalDateTime.now().plusDays(2),
            "Park", EventType.PUBLIC);
        invs.inviteFriend(ev.getEventId(), "bob");
        invs.revoke(ev.getEventId(), bob.getUserId());
        assertFalse(ev.hasInvited(bob.getUserId()));
    }

    @Test
    @DisplayName("inviteMany is partial-failure tolerant")
    void inviteManyPartial() {
        users.login("alice", "alice123");
        Event ev = events.createEvent("Meetup", "", LocalDateTime.now().plusDays(2),
            "Park", EventType.PUBLIC);
        // alice is the creator — self-invite should count as a failure, not abort batch
        BatchInviteResult r = invs.inviteMany(ev.getEventId(),
            List.of("bob", "alice", "carol", "ghost"));
        assertEquals(2, r.getInvitedCount());
        assertEquals(2, r.getFailureCount());
    }

    @Test
    @DisplayName("cancelled-event invite rejected")
    void cancelledInviteRejected() {
        users.login("alice", "alice123");
        Event ev = events.createEvent("Meetup", "", LocalDateTime.now().plusDays(2),
            "Park", EventType.PUBLIC);
        events.cancelEvent(ev.getEventId());
        assertThrows(InvalidOperationException.class,
            () -> invs.inviteFriend(ev.getEventId(), "bob"));
    }

    @Test
    @DisplayName("invitation pushes notification to invitee")
    void invitePushesNotification() {
        users.login("alice", "alice123");
        Event ev = events.createEvent("Meetup", "", LocalDateTime.now().plusDays(2),
            "Park", EventType.PUBLIC);
        int before = bob.getNotifications().size();
        invs.inviteFriend(ev.getEventId(), "bob");
        assertTrue(bob.getNotifications().size() > before);
    }

    @Test
    @DisplayName("viewInvitees authorization: only creator")
    void viewInviteesRequiresCreator() {
        users.login("alice", "alice123");
        Event ev = events.createEvent("Meetup", "", LocalDateTime.now().plusDays(2),
            "Park", EventType.PUBLIC);
        invs.inviteFriend(ev.getEventId(), "bob");
        users.logout();
        users.login("carol", "carol123");
        assertThrows(RuntimeException.class, () -> invs.viewInvitees(ev.getEventId()));
    }
}
