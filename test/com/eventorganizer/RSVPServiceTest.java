package com.eventorganizer;

import com.eventorganizer.exceptions.AuthorizationException;
import com.eventorganizer.exceptions.InvalidOperationException;
import com.eventorganizer.models.Event;
import com.eventorganizer.models.Invitation;
import com.eventorganizer.models.User;
import com.eventorganizer.models.enums.EventType;
import com.eventorganizer.models.enums.RSVPStatus;
import com.eventorganizer.services.EventService;
import com.eventorganizer.services.InvitationService;
import com.eventorganizer.services.RSVPService;
import com.eventorganizer.services.UserService;
import com.eventorganizer.store.TestHooks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RSVPServiceTest {

    private UserService users;
    private EventService events;
    private InvitationService invs;
    private RSVPService rsvp;
    private User alice;
    private User bob;
    private Event ev;

    @BeforeEach
    void reset() {
        TestHooks.reset();
        users = new UserService();
        events = new EventService();
        invs = new InvitationService();
        rsvp = new RSVPService();
        alice = users.register("alice", "a@example.com", "alice123");
        bob   = users.register("bob",   "b@example.com", "bob12345");
        users.register("carol", "c@example.com", "carol123");
        users.login("alice", "alice123");
        ev = events.createEvent("Meetup", "", LocalDateTime.now().plusDays(2),
            "Park", EventType.PUBLIC);
        invs.inviteFriend(ev.getEventId(), "bob");
    }

    @Test
    @DisplayName("RSVP ACCEPTED updates invitation")
    void respondAccepted() {
        users.logout();
        users.login("bob", "bob12345");
        rsvp.respond(ev.getEventId(), RSVPStatus.ACCEPTED);
        assertEquals(RSVPStatus.ACCEPTED, ev.getInvitationForUser(bob.getUserId()).getStatus());
    }

    @Test
    @DisplayName("RSVP DECLINED updates invitation")
    void respondDeclined() {
        users.logout();
        users.login("bob", "bob12345");
        rsvp.respond(ev.getEventId(), RSVPStatus.DECLINED);
        assertEquals(RSVPStatus.DECLINED, ev.getInvitationForUser(bob.getUserId()).getStatus());
    }

    @Test
    @DisplayName("non-invitee cannot respond")
    void nonInviteeRejected() {
        users.logout();
        users.login("carol", "carol123");
        assertThrows(InvalidOperationException.class,
            () -> rsvp.respond(ev.getEventId(), RSVPStatus.ACCEPTED));
    }

    @Test
    @DisplayName("respond on cancelled event is rejected")
    void cancelledRespondRejected() {
        events.cancelEvent(ev.getEventId());
        users.logout();
        users.login("bob", "bob12345");
        assertThrows(InvalidOperationException.class,
            () -> rsvp.respond(ev.getEventId(), RSVPStatus.ACCEPTED));
    }

    @Test
    @DisplayName("permissive re-response: invitee can switch status")
    void canSwitch() {
        users.logout();
        users.login("bob", "bob12345");
        rsvp.respond(ev.getEventId(), RSVPStatus.ACCEPTED);
        rsvp.respond(ev.getEventId(), RSVPStatus.MAYBE);
        assertEquals(RSVPStatus.MAYBE, ev.getInvitationForUser(bob.getUserId()).getStatus());
    }

    @Test
    @DisplayName("summary reports correct counts")
    void summaryCorrect() {
        users.logout();
        users.login("bob", "bob12345");
        rsvp.respond(ev.getEventId(), RSVPStatus.ACCEPTED);
        users.logout();
        users.login("alice", "alice123");
        Map<RSVPStatus, Long> counts = rsvp.viewRSVPSummary(ev.getEventId());
        assertEquals(1L, counts.get(RSVPStatus.ACCEPTED));
        assertEquals(0L, counts.get(RSVPStatus.DECLINED));
    }

    @Test
    @DisplayName("getInvitationsFor requires creator")
    void getInvitationsRequiresCreator() {
        users.logout();
        users.login("bob", "bob12345");
        assertThrows(AuthorizationException.class,
            () -> rsvp.getInvitationsFor(ev.getEventId()));
    }

    @Test
    @DisplayName("creator sees invitation list")
    void creatorSeesInvitations() {
        var list = rsvp.getInvitationsFor(ev.getEventId());
        assertEquals(1, list.size());
        Invitation inv = list.get(0);
        assertEquals(bob.getUserId(), inv.getInviteeId());
    }
}
