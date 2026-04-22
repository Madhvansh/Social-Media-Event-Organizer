package com.eventorganizer;

import com.eventorganizer.exceptions.AuthenticationException;
import com.eventorganizer.exceptions.DuplicateEmailException;
import com.eventorganizer.exceptions.DuplicateUsernameException;
import com.eventorganizer.exceptions.ValidationException;
import com.eventorganizer.models.User;
import com.eventorganizer.models.dto.UserProfileDTO;
import com.eventorganizer.services.UserService;
import com.eventorganizer.store.TestHooks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest {

    private UserService users;

    @BeforeEach
    void reset() {
        TestHooks.reset();
        users = new UserService();
    }

    @Test
    @DisplayName("register happy path")
    void registerHappyPath() {
        User u = users.register("alice", "alice@example.com", "alice123");
        assertNotNull(u.getUserId());
        assertEquals("alice", u.getUsername());
    }

    @Test
    @DisplayName("duplicate username is rejected")
    void duplicateUsernameRejected() {
        users.register("alice", "alice@example.com", "alice123");
        assertThrows(DuplicateUsernameException.class,
            () -> users.register("alice", "other@example.com", "pass1234"));
    }

    @Test
    @DisplayName("duplicate email is rejected")
    void duplicateEmailRejected() {
        users.register("alice", "alice@example.com", "alice123");
        assertThrows(DuplicateEmailException.class,
            () -> users.register("bob", "alice@example.com", "bob12345"));
    }

    @Test
    @DisplayName("invalid email format is rejected")
    void invalidEmailRejected() {
        assertThrows(ValidationException.class,
            () -> users.register("alice", "not-an-email", "alice123"));
    }

    @Test
    @DisplayName("weak password is rejected")
    void weakPasswordRejected() {
        assertThrows(ValidationException.class,
            () -> users.register("alice", "alice@example.com", "abc"));
    }

    @Test
    @DisplayName("login is case-insensitive on username")
    void loginCaseInsensitive() {
        users.register("Alice", "alice@example.com", "alice123");
        User u = users.login("alice", "alice123");
        assertEquals("Alice", u.getUsername());
    }

    @Test
    @DisplayName("login with wrong password throws AuthenticationException with generic message")
    void wrongPasswordUniformMessage() {
        users.register("alice", "alice@example.com", "alice123");
        AuthenticationException ex = assertThrows(AuthenticationException.class,
            () -> users.login("alice", "wrongpass"));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    @DisplayName("login with unknown username throws same generic message (anti-enumeration)")
    void unknownUsernameSameMessage() {
        AuthenticationException ex = assertThrows(AuthenticationException.class,
            () -> users.login("ghost", "anything123"));
        assertEquals("Invalid credentials", ex.getMessage());
    }

    @Test
    @DisplayName("changePassword happy path")
    void changePasswordHappyPath() {
        users.register("alice", "alice@example.com", "alice123");
        users.login("alice", "alice123");
        users.changePassword("alice123".toCharArray(), "newPass1".toCharArray());
        users.logout();
        assertNotNull(users.login("alice", "newPass1"));
    }

    @Test
    @DisplayName("changePassword with wrong current throws AuthenticationException")
    void changePasswordWrongCurrent() {
        users.register("alice", "alice@example.com", "alice123");
        users.login("alice", "alice123");
        assertThrows(AuthenticationException.class,
            () -> users.changePassword("wrongcurr".toCharArray(), "newPass1".toCharArray()));
    }

    @Test
    @DisplayName("getProfile returns populated DTO")
    void getProfileShape() {
        User u = users.register("alice", "alice@example.com", "alice123");
        UserProfileDTO p = users.getProfile(u);
        assertEquals("alice", p.getUsername());
        assertEquals("alice@example.com", p.getEmail());
        assertEquals(0, p.getFriendCount());
        assertEquals(0, p.getEventsCreated());
        assertTrue(p.getMemberSince() != null);
    }
}
