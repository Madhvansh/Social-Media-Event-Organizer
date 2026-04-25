package com.eventorganizer;

import com.eventorganizer.models.Event;
import com.eventorganizer.models.FriendRequest;
import com.eventorganizer.models.Invitation;
import com.eventorganizer.models.PublicEvent;
import com.eventorganizer.models.User;
import com.eventorganizer.models.enums.RSVPStatus;
import com.eventorganizer.services.UserService;
import com.eventorganizer.store.TestHooks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelInvariantsTest {

    @BeforeEach
    void reset() {
        TestHooks.reset();
    }

    @Test
    @DisplayName("User equality is by userId")
    void userEquality() {
        UserService us = new UserService();
        User a = us.register("alice", "a@example.com", "alice123");
        User b = us.register("bob",   "b@example.com", "bob12345");
        assertNotEquals(a, b);
        assertEquals(a.hashCode(), a.hashCode());
    }

    @Test
    @DisplayName("Event equality is by eventId")
    void eventEquality() {
        Event e1 = new PublicEvent("E1", "A", "", LocalDateTime.now().plusDays(1), "Home", "U1");
        Event e2 = new PublicEvent("E1", "Z", "", LocalDateTime.now().plusDays(5), "Away", "U2");
        Event e3 = new PublicEvent("E2", "A", "", LocalDateTime.now().plusDays(1), "Home", "U1");
        assertEquals(e1, e2);
        assertNotEquals(e1, e3);
    }

    @Test
    @DisplayName("Invitation equality is composite (eventId, inviteeId)")
    void invitationEqualityComposite() {
        Invitation a = new Invitation("I1", "E1", "U1");
        Invitation b = new Invitation("I2", "E1", "U1");
        Invitation c = new Invitation("I3", "E1", "U2");
        assertEquals(a, b);
        assertNotEquals(a, c);
    }

    @Test
    @DisplayName("FriendRequest equality is by requestId")
    void friendRequestEquality() {
        FriendRequest a = new FriendRequest("R1", "U1", "U2");
        FriendRequest b = new FriendRequest("R1", "U9", "U9");
        FriendRequest c = new FriendRequest("R2", "U1", "U2");
        assertEquals(a, b);
        assertNotEquals(a, c);
    }

    @Test
    @DisplayName("toString is non-null on every model")
    void toStringPresent() {
        assertNotNull(new PublicEvent("E1", "A", "", LocalDateTime.now().plusDays(1), "X", "U1").toString());
        assertNotNull(new Invitation("I1", "E1", "U1").toString());
        assertNotNull(new FriendRequest("R1", "U1", "U2").toString());
    }

    @Test
    @DisplayName("Primary-ID fields are declared final on every model")
    void primaryIdsFinal() throws NoSuchFieldException {
        assertFinal(User.class,          "userId");
        assertFinal(Event.class,         "eventId");
        assertFinal(Invitation.class,    "invitationId");
        assertFinal(FriendRequest.class, "requestId");
    }

    @Test
    @DisplayName("Invitation timestamps: sentAt is final and set on construction")
    void invitationTimestampsImmutable() throws NoSuchFieldException {
        assertFinal(Invitation.class, "sentAt");
        Invitation inv = new Invitation("I1", "E1", "U1");
        assertNotNull(inv.getSentAt());
    }

    @Test
    @DisplayName("Invitation respond transitions to target status")
    void invitationRespondTransition() {
        Invitation inv = new Invitation("I1", "E1", "U1");
        inv.respond(RSVPStatus.ACCEPTED);
        assertEquals(RSVPStatus.ACCEPTED, inv.getStatus());
        assertNotNull(inv.getRespondedAt());
    }

    @Test
    @DisplayName("Reportable: both Event and User implement it")
    void reportableOnBoth() {
        Event e = new PublicEvent("E1", "A", "", LocalDateTime.now().plusDays(1), "X", "U1");
        UserService us = new UserService();
        User u = us.register("alice", "a@example.com", "alice123");
        assertTrue(e instanceof com.eventorganizer.interfaces.Reportable);
        assertTrue(u instanceof com.eventorganizer.interfaces.Reportable);
    }

    @Test
    @DisplayName("Event.removeInvitation uses Invitation.equals (composite)")
    void invitationRemoveHonorsCompositeEquals() {
        Event e = new PublicEvent("E1", "A", "", LocalDateTime.now().plusDays(1), "X", "U1");
        Invitation a = new Invitation("I1", "E1", "U1");
        e.addInvitation(a);
        // A second Invitation with a different id but the same (eventId, inviteeId) is
        // equal() to the one we added; Event.removeInvitation must succeed.
        Invitation sameIdentity = new Invitation("I2", "E1", "U1");
        assertTrue(e.removeInvitation(sameIdentity));
        assertTrue(e.getInvitations().isEmpty());
    }

    @Test
    @DisplayName("New passwords are stored with the PBKDF2 prefix")
    void passwordHashHasPBKDF2Prefix() {
        UserService us = new UserService();
        User u = us.register("alice", "a@example.com", "alice123");
        assertTrue(u.getPasswordHash().startsWith("pbkdf2$"),
            "password hash must start with 'pbkdf2$' but was: " + u.getPasswordHash());
    }

    private static void assertFinal(Class<?> cls, String fieldName) throws NoSuchFieldException {
        Field f = cls.getDeclaredField(fieldName);
        assertTrue(Modifier.isFinal(f.getModifiers()),
            cls.getSimpleName() + "." + fieldName + " must be final");
    }
}
