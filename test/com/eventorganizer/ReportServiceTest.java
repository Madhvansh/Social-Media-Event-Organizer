package com.eventorganizer;

import com.eventorganizer.models.Event;
import com.eventorganizer.models.User;
import com.eventorganizer.models.dto.UserActivityReport;
import com.eventorganizer.models.enums.EventType;
import com.eventorganizer.models.enums.RSVPStatus;
import com.eventorganizer.services.EventService;
import com.eventorganizer.services.InvitationService;
import com.eventorganizer.services.ReportService;
import com.eventorganizer.services.RSVPService;
import com.eventorganizer.services.UserService;
import com.eventorganizer.store.TestHooks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReportServiceTest {

    private UserService users;
    private EventService events;
    private InvitationService invs;
    private RSVPService rsvp;
    private ReportService reports;
    private User alice;

    @BeforeEach
    void reset() {
        TestHooks.reset();
        users = new UserService();
        events = new EventService();
        invs = new InvitationService();
        rsvp = new RSVPService();
        reports = new ReportService();
        alice = users.register("alice", "a@example.com", "alice123");
        users.register("bob", "b@example.com", "bob12345");
        users.login("alice", "alice123");
    }

    @Test
    @DisplayName("empty activity report for fresh user")
    void emptyReport() {
        UserActivityReport r = reports.buildUserActivity(alice);
        assertEquals(0, r.getTotalEventsCreated());
        assertEquals(0, r.getUpcomingEvents());
        assertEquals(0, r.getPastEvents());
        assertEquals(0, r.getCancelledEvents());
    }

    @Test
    @DisplayName("report invariant: total = upcoming + past + cancelled")
    void invariantHolds() {
        events.createEvent("A", "", LocalDateTime.now().plusDays(1), "Home", EventType.PUBLIC);
        events.createEvent("B", "", LocalDateTime.now().plusDays(2), "Home", EventType.PUBLIC);
        Event c = events.createEvent("C", "", LocalDateTime.now().plusDays(3), "Home", EventType.PUBLIC);
        events.cancelEvent(c.getEventId());
        UserActivityReport r = reports.buildUserActivity(alice);
        assertEquals(r.getTotalEventsCreated(),
            r.getUpcomingEvents() + r.getPastEvents() + r.getCancelledEvents());
    }

    @Test
    @DisplayName("confirmed attendees count reflects accepted RSVPs")
    void attendeesCountAccepted() {
        Event e = events.createEvent("A", "", LocalDateTime.now().plusDays(2), "Home", EventType.PUBLIC);
        invs.inviteFriend(e.getEventId(), "bob");
        users.logout();
        users.login("bob", "bob12345");
        rsvp.respond(e.getEventId(), RSVPStatus.ACCEPTED);
        users.logout();
        users.login("alice", "alice123");
        UserActivityReport r = reports.buildUserActivity(alice);
        assertEquals(1L, r.getTotalConfirmedAttendees());
    }

    @Test
    @DisplayName("per-event section lists every event")
    void perEventPresent() {
        events.createEvent("A", "", LocalDateTime.now().plusDays(1), "Home", EventType.PUBLIC);
        events.createEvent("B", "", LocalDateTime.now().plusDays(2), "Home", EventType.PUBLIC);
        UserActivityReport r = reports.buildUserActivity(alice);
        assertEquals(2, r.getPerEvent().size());
    }

    @Test
    @DisplayName("generateSummary polymorphism: Event Reportable path")
    void eventSummaryNonNull() {
        Event e = events.createEvent("A", "", LocalDateTime.now().plusDays(1),
            "Home", EventType.PUBLIC);
        String s = reports.generateEventSummary(e);
        assertNotNull(s);
    }

    @Test
    @DisplayName("generateSummary polymorphism: User Reportable path")
    void userSummaryNonNull() {
        String s = reports.generateEventSummary(alice);
        assertNotNull(s);
    }
}
