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
import com.eventorganizer.utils.Normalize;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public enum DataStore {
    INSTANCE;

    private final ConcurrentHashMap<String, User> usersById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> usernameIndex = new ConcurrentHashMap<>();   // normalized username -> userId
    private final ConcurrentHashMap<String, String> emailIndex = new ConcurrentHashMap<>();      // normalized email    -> userId
    private final ConcurrentHashMap<String, Event> eventsById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, FriendRequest> friendRequestsById = new ConcurrentHashMap<>();
    // eventId -> (inviteeId -> Invitation)
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Invitation>> invitationIndex = new ConcurrentHashMap<>();
    // min(id1,id2) + "|" + max(id1,id2) -> latest requestId between that pair
    private final ConcurrentHashMap<String, String> friendRequestBetweenIndex = new ConcurrentHashMap<>();
    // creatorUserId -> set of eventIds (A8)
    private final ConcurrentHashMap<String, Set<String>> eventsByCreator = new ConcurrentHashMap<>();

    private volatile User currentUser;
    private volatile boolean seeded = false;
    private volatile Clock clock = Clock.systemDefaultZone();

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
        usernameIndex.put(Normalize.identifier(u.getUsername()), u.getUserId());
        if (u.getEmail() != null) {
            emailIndex.put(Normalize.identifier(u.getEmail()), u.getUserId());
        }
    }

    public Optional<User> findUserById(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(usersById.get(id));
    }

    public Optional<User> findUserByUsername(String username) {
        if (username == null) return Optional.empty();
        String id = usernameIndex.get(Normalize.identifier(username));
        return id == null ? Optional.empty() : Optional.ofNullable(usersById.get(id));
    }

    public Optional<User> findUserByEmail(String email) {
        if (email == null) return Optional.empty();
        String id = emailIndex.get(Normalize.identifier(email));
        return id == null ? Optional.empty() : Optional.ofNullable(usersById.get(id));
    }

    public boolean usernameExists(String username) {
        return username != null && usernameIndex.containsKey(Normalize.identifier(username));
    }

    public boolean emailExists(String email) {
        return email != null && emailIndex.containsKey(Normalize.identifier(email));
    }

    public Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(usersById.values());
    }

    // --- Events ---
    public void saveEvent(Event e) {
        eventsById.put(e.getEventId(), e);
        invitationIndex.computeIfAbsent(e.getEventId(), k -> new ConcurrentHashMap<>());
        if (e.getCreatorId() != null) {
            eventsByCreator
                .computeIfAbsent(e.getCreatorId(), k -> ConcurrentHashMap.newKeySet())
                .add(e.getEventId());
        }
    }

    public Optional<Event> findEventById(String id) {
        return id == null ? Optional.empty() : Optional.ofNullable(eventsById.get(id));
    }

    public Collection<Event> getAllEvents() {
        return Collections.unmodifiableCollection(eventsById.values());
    }

    /** O(K) event-id lookup by creator (A8). Returns a snapshot view. */
    public Set<String> eventIdsForCreator(String creatorId) {
        if (creatorId == null) return Collections.emptySet();
        Set<String> ids = eventsByCreator.get(creatorId);
        return ids == null ? Collections.emptySet() : Collections.unmodifiableSet(ids);
    }

    // --- Invitations (index-only; Event also holds its own list) ---
    public void indexInvitation(Invitation inv) {
        if (inv == null) return;
        invitationIndex
            .computeIfAbsent(inv.getEventId(), k -> new ConcurrentHashMap<>())
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
    synchronized void resetForTests() {
        usersById.clear();
        usernameIndex.clear();
        emailIndex.clear();
        eventsById.clear();
        friendRequestsById.clear();
        invitationIndex.clear();
        friendRequestBetweenIndex.clear();
        eventsByCreator.clear();
        currentUser = null;
        seeded = false;
        clock = Clock.systemDefaultZone();
        com.eventorganizer.utils.IdGenerator.resetForTests();
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
