package com.eventorganizer.store;

import com.eventorganizer.models.Event;
import com.eventorganizer.models.FriendRequest;
import com.eventorganizer.models.Invitation;
import com.eventorganizer.models.User;
import com.eventorganizer.models.enums.EventType;
import com.eventorganizer.models.enums.RSVPStatus;
import com.eventorganizer.services.EventService;
import com.eventorganizer.services.FriendService;
import com.eventorganizer.services.InvitationService;
import com.eventorganizer.services.RSVPService;
import com.eventorganizer.services.UserService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum DataStore {
    INSTANCE;

    private final Map<String, User> usersById = new HashMap<>();
    private final Map<String, String> usernameIndex = new HashMap<>();   // lowercase username -> userId
    private final Map<String, String> emailIndex = new HashMap<>();      // lowercase email    -> userId
    private final Map<String, Event> eventsById = new HashMap<>();
    private final Map<String, FriendRequest> friendRequestsById = new HashMap<>();
    // eventId -> (inviteeId -> Invitation)
    private final Map<String, Map<String, Invitation>> invitationIndex = new HashMap<>();
    // min(id1,id2) + "|" + max(id1,id2) -> latest requestId between that pair
    private final Map<String, String> friendRequestBetweenIndex = new HashMap<>();

    private User currentUser;
    private boolean seeded = false;
    private Clock clock = Clock.systemDefaultZone();

    // --- Clock ---
    public Clock getClock() { return clock; }
    /** Package-private for tests only. */
    void setClock(Clock c) { this.clock = c == null ? Clock.systemDefaultZone() : c; }

    /** Null-safe static accessor used by utility code that cannot depend on singleton init order. */
    public static Clock getClockOrDefault() {
        try {
            Clock c = INSTANCE.clock;
            return c == null ? Clock.systemDefaultZone() : c;
        } catch (Throwable t) {
            return Clock.systemDefaultZone();
        }
    }

    // --- Users ---
    public void saveUser(User u) {
        usersById.put(u.getUserId(), u);
        usernameIndex.put(u.getUsername().toLowerCase(), u.getUserId());
        if (u.getEmail() != null) {
            emailIndex.put(u.getEmail().toLowerCase(), u.getUserId());
        }
    }

    public Optional<User> findUserById(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(usersById.get(id));
    }

    public Optional<User> findUserByUsername(String username) {
        if (username == null) return Optional.empty();
        String id = usernameIndex.get(username.toLowerCase());
        return id == null ? Optional.empty() : Optional.ofNullable(usersById.get(id));
    }

    public Optional<User> findUserByEmail(String email) {
        if (email == null) return Optional.empty();
        String id = emailIndex.get(email.toLowerCase());
        return id == null ? Optional.empty() : Optional.ofNullable(usersById.get(id));
    }

    public boolean usernameExists(String username) {
        return username != null && usernameIndex.containsKey(username.toLowerCase());
    }

    public boolean emailExists(String email) {
        return email != null && emailIndex.containsKey(email.toLowerCase());
    }

    public Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(usersById.values());
    }

    // --- Events ---
    public void saveEvent(Event e) {
        eventsById.put(e.getEventId(), e);
        invitationIndex.computeIfAbsent(e.getEventId(), k -> new HashMap<>());
    }

    public Optional<Event> findEventById(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(eventsById.get(id));
    }

    public Collection<Event> getAllEvents() {
        return Collections.unmodifiableCollection(eventsById.values());
    }

    // --- Invitations (index-only; Event also holds its own list) ---
    public void indexInvitation(Invitation inv) {
        if (inv == null) return;
        invitationIndex
            .computeIfAbsent(inv.getEventId(), k -> new HashMap<>())
            .put(inv.getInviteeId(), inv);
    }

    public void unindexInvitation(String eventId, String inviteeId) {
        if (eventId == null || inviteeId == null) return;
        Map<String, Invitation> m = invitationIndex.get(eventId);
        if (m != null) m.remove(inviteeId);
    }

    public Optional<Invitation> findInvitation(String eventId, String inviteeId) {
        if (eventId == null || inviteeId == null) return Optional.empty();
        Map<String, Invitation> m = invitationIndex.get(eventId);
        return m == null ? Optional.empty() : Optional.ofNullable(m.get(inviteeId));
    }

    public boolean hasInvitation(String eventId, String inviteeId) {
        return findInvitation(eventId, inviteeId).isPresent();
    }

    // --- Friend requests ---
    public void saveFriendRequest(FriendRequest r) {
        friendRequestsById.put(r.getRequestId(), r);
        friendRequestBetweenIndex.put(
            pairKey(r.getSenderId(), r.getReceiverId()),
            r.getRequestId());
    }

    public Optional<FriendRequest> findFriendRequestById(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(friendRequestsById.get(id));
    }

    public Optional<FriendRequest> findLatestFriendRequestBetween(String a, String b) {
        if (a == null || b == null) return Optional.empty();
        String rid = friendRequestBetweenIndex.get(pairKey(a, b));
        return rid == null ? Optional.empty() : Optional.ofNullable(friendRequestsById.get(rid));
    }

    public Collection<FriendRequest> getAllFriendRequests() {
        return Collections.unmodifiableCollection(friendRequestsById.values());
    }

    private static String pairKey(String a, String b) {
        return a.compareTo(b) <= 0 ? a + "|" + b : b + "|" + a;
    }

    // --- Session ---
    public User getCurrentUser()       { return currentUser; }
    public void setCurrentUser(User u) { this.currentUser = u; }
    public void clearSession()         { this.currentUser = null; }
    public boolean isLoggedIn()        { return currentUser != null; }

    /** Package-private: test harness hook. */
    void resetForTests() {
        usersById.clear();
        usernameIndex.clear();
        emailIndex.clear();
        eventsById.clear();
        friendRequestsById.clear();
        invitationIndex.clear();
        friendRequestBetweenIndex.clear();
        currentUser = null;
        seeded = false;
        clock = Clock.systemDefaultZone();
    }

    public synchronized void seed() {
        if (seeded) return;
        seeded = true;

        UserService us = new UserService();
        User alice = us.register("alice", "alice@example.com", "alice123");
        User bob   = us.register("bob",   "bob@example.com",   "bob123");
        User carol = us.register("carol", "carol@example.com", "carol123");

        alice.addFriend(bob);
        bob.addFriend(alice);

        EventService es = new EventService();
        InvitationService is = new InvitationService();
        FriendService fs = new FriendService();
        RSVPService rs = new RSVPService();

        User previousSession = currentUser;
        LocalDateTime now = LocalDateTime.now(clock);

        // Alice creates her two events.
        setCurrentUser(alice);
        Event meetup = es.createEvent("Community Meetup",
            "Open to all - come meet your neighbors.",
            now.plusDays(7),
            "Central Park",
            EventType.PUBLIC);
        Event housewarming = es.createEvent("Alice's Housewarming",
            "Friends-only housewarming party.",
            now.plusDays(14),
            "Alice's Home",
            EventType.PRIVATE);
        // Alice invites bob to her private housewarming (pending invite for bob).
        is.inviteFriend(housewarming.getEventId(), "bob");

        // Bob creates a public event and invites alice.
        setCurrentUser(bob);
        Event gameNight = es.createEvent("Board Game Night",
            "Casual games and snacks - open to friends.",
            now.plusDays(3),
            "Bob's Place",
            EventType.PUBLIC);
        is.inviteFriend(gameNight.getEventId(), "alice");

        // Alice accepts bob's invitation (gives bob an RSVP notification).
        setCurrentUser(alice);
        rs.respond(gameNight.getEventId(), RSVPStatus.ACCEPTED);

        // Carol sends alice a pending friend request.
        setCurrentUser(carol);
        fs.sendFriendRequest("alice");

        // Alice's meetup is also public; invite carol so Discover shows content for her too.
        setCurrentUser(alice);
        is.inviteFriend(meetup.getEventId(), "carol");

        setCurrentUser(previousSession);
    }
}
