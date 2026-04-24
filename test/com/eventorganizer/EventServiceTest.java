package com.eventorganizer;

import com.eventorganizer.exceptions.AuthorizationException;
import com.eventorganizer.exceptions.InvalidOperationException;
import com.eventorganizer.exceptions.UnauthorizedException;
import com.eventorganizer.models.Event;
import com.eventorganizer.models.User;
import com.eventorganizer.models.enums.EventStatus;
import com.eventorganizer.models.enums.EventType;
import com.eventorganizer.services.EventService;
import com.eventorganizer.services.InvitationService;
import com.eventorganizer.services.UserService;
import com.eventorganizer.store.DataStore;
import com.eventorganizer.store.TestHooks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventServiceTest {

    private UserService users;
    private EventService events;
    private User alice;
    private User bob;

    @BeforeEach
    void reset() {
        TestHooks.reset();
        users = new UserService();
        events = new EventService();
        alice = users.register("alice", "alice@example.com", "alice123");
        bob   = users.register("bob",   "bob@example.com",   "bob12345");
        users.login("alice", "alice123");
    }

    @Test
    @DisplayName("createEvent happy path")
    void createHappyPath() {
        Event e = events.createEvent("Party", "desc",
            LocalDateTime.now().plusDays(2), "Home", EventType.PUBLIC);
        assertNotNull(e.getEventId());
        assertEquals(EventType.PUBLIC, e.getType());
    }

    @Test
    @DisplayName("createEvent with past date is rejected")
    void pastDateRejected() {
        assertThrows(InvalidOperationException.class, () ->
            events.createEvent("Party", "desc",
                LocalDateTime.now().minusDays(1), "Home", EventType.PUBLIC));
    }

    @Test
    @DisplayName("createEvent with far-future date is rejected")
    void farFutureRejected() {
        assertThrows(InvalidOperationException.class, () ->
            events.createEvent("Party", "desc",
                LocalDateTime.now().plusYears(20), "Home", EventType.PUBLIC));
    }

    @Test
    @DisplayName("editEvent authorization: only creator may edit")
    void editRequiresCreator() {
        Event e = events.createEvent("Party", "desc",
            LocalDateTime.now().plusDays(2), "Home", EventType.PUBLIC);
        users.logout();
        users.login("bob", "bob12345");
        assertThrows(AuthorizationException.class, () ->
            events.editEvent(e.getEventId(), "Hacked", null, null, null));
    }

    @Test
    @DisplayName("editEvent rejects far-future date")
    void editRejectsFarFuture() {
        Event e = events.createEvent("Party", "desc",
            LocalDateTime.now().plusDays(2), "Home", EventType.PUBLIC);
        assertThrows(InvalidOperationException.class, () ->
            events.editEvent(e.getEventId(), null, null,
                LocalDateTime.now().plusYears(20), null));
    }

    @Test
    @DisplayName("cancelEvent is idempotent")
    void cancelIdempotent() {
        Event e = events.createEvent("Party", "desc",
            LocalDateTime.now().plusDays(2), "Home", EventType.PUBLIC);
        events.cancelEvent(e.getEventId());
        // Second cancel is a silent no-op.
        events.cancelEvent(e.getEventId());
        assertEquals(EventStatus.CANCELLED, e.getStatus());
    }

    @Test
    @DisplayName("cancelEvent notifies all invitees")
    void cancelNotifiesInvitees() {
        alice.addFriend(bob);
        bob.addFriend(alice);
        Event e = events.createEvent("Party", "desc",
            LocalDateTime.now().plusDays(2), "Home", EventType.PUBLIC);
        new InvitationService().inviteFriend(e.getEventId(), "bob");
        int before = bob.getNotifications().size();
        events.cancelEvent(e.getEventId(), "venue gone");
        assertTrue(bob.getNotifications().size() > before);
    }

    @Test
    @DisplayName("createEventWithWarnings flags conflicting event within window")
    void conflictWarning() {
        LocalDateTime when = LocalDateTime.now().plusDays(2);
        events.createEvent("Party A", "desc", when, "Home", EventType.PUBLIC);
        EventService.CreateEventResult r = events.createEventWithWarnings(
            "Party B", "desc", when.plusMinutes(15), "Cafe", EventType.PUBLIC);
        assertFalse(r.getWarnings().isEmpty());
    }

    @Test
    @DisplayName("viewMyEvents returns only the caller's events")
    void viewMineOnly() {
        events.createEvent("A", "", LocalDateTime.now().plusDays(2), "Home", EventType.PUBLIC);
        users.logout();
        users.login("bob", "bob12345");
        events.createEvent("B", "", LocalDateTime.now().plusDays(2), "Home", EventType.PUBLIC);
        assertEquals(1, events.viewMyEvents().size());
        users.logout();
        users.login("alice", "alice123");
        assertEquals(1, events.viewMyEvents().size());
    }

    @Test
    @DisplayName("createEvent requires login")
    void createRequiresLogin() {
        users.logout();
        assertThrows(UnauthorizedException.class, () ->
            events.createEvent("X", "", LocalDateTime.now().plusDays(1),
                "Home", EventType.PUBLIC));
    }

    @Test
    @DisplayName("DataStore saves events")
    void dataStoreHoldsEvent() {
        Event e = events.createEvent("Party", "desc",
            LocalDateTime.now().plusDays(2), "Home", EventType.PUBLIC);
        assertTrue(DataStore.INSTANCE.findEventById(e.getEventId()).isPresent());
    }
}
