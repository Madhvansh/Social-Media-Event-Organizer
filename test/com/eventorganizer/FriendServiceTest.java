package com.eventorganizer;

import com.eventorganizer.exceptions.InvalidOperationException;
import com.eventorganizer.models.FriendRequest;
import com.eventorganizer.models.User;
import com.eventorganizer.models.enums.FriendRequestStatus;
import com.eventorganizer.services.FriendService;
import com.eventorganizer.services.UserService;
import com.eventorganizer.store.TestHooks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FriendServiceTest {

    private UserService users;
    private FriendService friends;
    private User alice;
    private User bob;

    @BeforeEach
    void reset() {
        TestHooks.reset();
        users = new UserService();
        friends = new FriendService();
        alice = users.register("alice", "alice@example.com", "alice123");
        bob   = users.register("bob",   "bob@example.com",   "bob12345");
    }

    @Test
    @DisplayName("send/accept/remove friendship happy path")
    void happyPath() {
        users.login("alice", "alice123");
        FriendRequest r = friends.sendFriendRequest("bob");
        users.logout();
        users.login("bob", "bob12345");
        friends.acceptFriendRequest(r.getRequestId());
        assertTrue(alice.isFriendWith(bob.getUserId()));
        assertTrue(bob.isFriendWith(alice.getUserId()));
        friends.removeFriend("alice");
        assertFalse(bob.isFriendWith(alice.getUserId()));
        assertFalse(alice.isFriendWith(bob.getUserId()));
    }

    @Test
    @DisplayName("self-request is blocked")
    void noSelfRequest() {
        users.login("alice", "alice123");
        assertThrows(InvalidOperationException.class, () -> friends.sendFriendRequest("alice"));
    }

    @Test
    @DisplayName("duplicate pending request is blocked")
    void dupPendingBlocked() {
        users.login("alice", "alice123");
        friends.sendFriendRequest("bob");
        assertThrows(InvalidOperationException.class, () -> friends.sendFriendRequest("bob"));
    }

    @Test
    @DisplayName("reject flips status and allows no immediate re-request")
    void rejectCooldown() {
        users.login("alice", "alice123");
        FriendRequest r = friends.sendFriendRequest("bob");
        users.logout();
        users.login("bob", "bob12345");
        friends.rejectFriendRequest(r.getRequestId());
        assertEquals(FriendRequestStatus.REJECTED, r.getStatus());
        users.logout();
        users.login("alice", "alice123");
        // Within cooldown window (default 24h) — sendRequest to bob must fail.
        assertThrows(InvalidOperationException.class, () -> friends.sendFriendRequest("bob"));
    }

    @Test
    @DisplayName("withdraw a pending request transitions to WITHDRAWN")
    void withdraw() {
        users.login("alice", "alice123");
        FriendRequest r = friends.sendFriendRequest("bob");
        friends.cancelSentRequest(r.getRequestId());
        assertEquals(FriendRequestStatus.WITHDRAWN, r.getStatus());
    }

    @Test
    @DisplayName("listIncomingRequests returns only pending")
    void listPending() {
        users.login("alice", "alice123");
        FriendRequest r = friends.sendFriendRequest("bob");
        users.logout();
        users.login("bob", "bob12345");
        assertEquals(1, friends.listIncomingRequests().size());
        friends.rejectFriendRequest(r.getRequestId());
        assertEquals(0, friends.listIncomingRequests().size());
    }

    @Test
    @DisplayName("accept both-sides symmetry")
    void acceptBothSides() {
        users.login("alice", "alice123");
        FriendRequest r = friends.sendFriendRequest("bob");
        users.logout();
        users.login("bob", "bob12345");
        friends.acceptFriendRequest(r.getRequestId());
        assertTrue(alice.isFriendWith(bob.getUserId()));
        assertTrue(bob.isFriendWith(alice.getUserId()));
    }

    @Test
    @DisplayName("listFriends sorted alphabetically")
    void listFriendsAlpha() {
        User c = users.register("charlie", "c@example.com", "char1234");
        users.login("alice", "alice123");
        alice.addFriend(bob);
        alice.addFriend(c);
        var list = friends.listFriends();
        assertEquals("bob", list.get(0).getUsername());
        assertEquals("charlie", list.get(1).getUsername());
    }
}
