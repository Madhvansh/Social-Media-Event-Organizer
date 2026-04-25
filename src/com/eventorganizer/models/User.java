package com.eventorganizer.models;

import com.eventorganizer.interfaces.Reportable;
import com.eventorganizer.store.DataStore;
import com.eventorganizer.utils.Limits;
import com.eventorganizer.utils.PasswordHasher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class User implements Reportable, java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final String userId;
    private final String username;
    private String email;
    private String passwordHash;
    private final byte[] salt;
    private String bio;
    private final LocalDateTime createdAt;

    private final Set<String> friendIds;
    private final List<String> incomingFriendRequestIds;
    private final List<String> outgoingFriendRequestIds;
    private final List<Notification> notifications;

    public User(String userId, String username, String email, String passwordHash, byte[] salt) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.username = Objects.requireNonNull(username, "username");
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt == null ? new byte[0] : salt.clone();
        this.bio = "";
        this.createdAt = LocalDateTime.now(DataStore.INSTANCE.getClock());
        this.friendIds = new LinkedHashSet<>();
        this.incomingFriendRequestIds = new ArrayList<>();
        this.outgoingFriendRequestIds = new ArrayList<>();
        this.notifications = Collections.synchronizedList(new ArrayList<>());
    }

    public String getUserId()            { return userId; }
    public String getUsername()          { return username; }
    public String getEmail()             { return email; }
    public String getBio()               { return bio; }
    public String getPasswordHash()      { return passwordHash; }
    public byte[] getSalt()              { return salt.clone(); }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    public void setEmail(String email)        { this.email = email; }
    public void setBio(String bio)            { this.bio = bio; }
    public void setPasswordHash(String hash)  { this.passwordHash = hash; }

    public void updateProfile(String email, String bio) {
        if (email != null) this.email = email;
        if (bio != null)   this.bio = bio;
    }

    public boolean verifyPassword(String rawPassword) {
        return PasswordHasher.verify(rawPassword, salt, passwordHash);
    }

    public boolean verifyPassword(char[] rawPassword) {
        return PasswordHasher.verify(rawPassword, salt, passwordHash);
    }

    public void addFriend(User other) {
        if (other == null || other.userId.equals(this.userId)) return;
        friendIds.add(other.userId);
    }

    public void removeFriend(String otherUserId) {
        if (otherUserId == null) return;
        friendIds.remove(otherUserId);
    }

    public boolean isFriendWith(String otherUserId) {
        if (otherUserId == null || otherUserId.isEmpty()) return false;
        return friendIds.contains(otherUserId);
    }

    public int getFriendCount() {
        return friendIds.size();
    }

    public List<String> getFriendIds() {
        return new ArrayList<>(friendIds);
    }

    public void addIncomingFriendRequest(String requestId) {
        if (!incomingFriendRequestIds.contains(requestId)) incomingFriendRequestIds.add(requestId);
    }

    public void removeIncomingFriendRequest(String requestId) {
        incomingFriendRequestIds.remove(requestId);
    }

    public List<String> getIncomingFriendRequestIds() {
        return new ArrayList<>(incomingFriendRequestIds);
    }

    public void addOutgoingFriendRequest(String requestId) {
        if (!outgoingFriendRequestIds.contains(requestId)) outgoingFriendRequestIds.add(requestId);
    }

    public void removeOutgoingFriendRequest(String requestId) {
        outgoingFriendRequestIds.remove(requestId);
    }

    public List<String> getOutgoingFriendRequestIds() {
        return new ArrayList<>(outgoingFriendRequestIds);
    }

    /* deletes oldest read notif if user has reached limit, never deletes UNread notifs */
    public boolean addNotification(Notification n) {
        if (n == null) return false;
        int cap = Limits.NOTIFICATIONS_PER_USER_MAX;
        synchronized (notifications) {
            if (notifications.size() >= cap) {
                int evictIdx = -1;
                for (int i = 0; i < notifications.size(); i++) {
                    if (notifications.get(i).isRead()) { evictIdx = i; break; }
                }
                if (evictIdx < 0) return false;
                notifications.remove(evictIdx);
            }
            notifications.add(n);
            return true;
        }
    }

    public int removeNotificationsMatching(Predicate<Notification> test) {
        if (test == null) return 0;
        int removed = 0;
        synchronized (notifications) {
            Iterator<Notification> it = notifications.iterator();
            while (it.hasNext()) {
                if (test.test(it.next())) { it.remove(); removed++; }
            }
        }
        return removed;
    }

    public void markAllNotificationsAsRead() {
        synchronized (notifications) {
            for (Notification n : notifications) n.markAsRead();
        }
    }

    public List<Notification> getNotifications() {
        synchronized (notifications) {
            return new ArrayList<>(notifications);
        }
    }

    public List<Notification> getUnreadNotifications() {
        synchronized (notifications) {
            return notifications.stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());
        }
    }

    @Override
    public String generateSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("User: ").append(username).append("\n");
        sb.append("  ID:           ").append(userId).append("\n");
        sb.append("  Email:        ").append(email == null ? "-" : email).append("\n");
        sb.append("  Member since: ").append(createdAt).append("\n");
        sb.append("  Friends:      ").append(friendIds.size()).append("\n");
        sb.append("  Pending in:   ").append(incomingFriendRequestIds.size()).append("\n");
        sb.append("  Pending out:  ").append(outgoingFriendRequestIds.size()).append("\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return userId.equals(((User) o).userId);
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

    @Override
    public String toString() {
        return "User{id=" + userId + ", username=" + username + "}";
    }
}
