package com.eventorganizer.services;

import com.eventorganizer.exceptions.AuthorizationException;
import com.eventorganizer.exceptions.InvalidOperationException;
import com.eventorganizer.exceptions.UnauthorizedException;
import com.eventorganizer.exceptions.UserNotFoundException;
import com.eventorganizer.models.FriendRequest;
import com.eventorganizer.models.FriendRequestNotification;
import com.eventorganizer.models.User;
import com.eventorganizer.models.enums.FriendRequestStatus;
import com.eventorganizer.store.DataStore;
import com.eventorganizer.utils.IdGenerator;
import com.eventorganizer.utils.Limits;
import com.eventorganizer.utils.Validator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FriendService {

    private final NotificationService notifications = new NotificationService();

    public FriendRequest sendFriendRequest(String targetUsername) {
        Validator.requireNonBlank(targetUsername, "Username");
        Validator.requireLength(targetUsername, Limits.USERNAME_MAX, "Username");
        User sender = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;
        User target = ds.findUserByUsername(targetUsername)
            .orElseThrow(() -> new UserNotFoundException("No user named '" + targetUsername + "'."));
        if (target.getUserId().equals(sender.getUserId())) {
            throw new InvalidOperationException("You cannot friend yourself.");
        }
        if (sender.isFriendWith(target.getUserId())) {
            throw new InvalidOperationException("You are already friends with " + target.getUsername() + ".");
        }
        FriendRequest prior = ds.findLatestFriendRequestBetween(
            sender.getUserId(), target.getUserId()).orElse(null);
        if (prior != null && prior.getSenderId().equals(sender.getUserId())) {
            if (prior.getStatus() == FriendRequestStatus.PENDING) {
                throw new InvalidOperationException("A pending request already exists.");
            }
            if (prior.getStatus() == FriendRequestStatus.REJECTED
                && prior.getResolvedAt() != null) {
                long hoursSince = Duration.between(
                        prior.getResolvedAt(),
                        LocalDateTime.now(DataStore.INSTANCE.getClock()))
                    .toHours();
                long cooldown = Limits.FRIEND_REQUEST_COOLDOWN_HOURS;
                if (hoursSince < cooldown) {
                    long remaining = Math.max(1, cooldown - hoursSince);
                    throw new InvalidOperationException(
                        "You can send another request in " + remaining + " hour"
                            + (remaining == 1 ? "" : "s") + ".");
                }
            }
        }

        FriendRequest req = new FriendRequest(
            IdGenerator.nextFriendRequestId(),
            sender.getUserId(),
            target.getUserId());
        ds.saveFriendRequest(req);
        try {
            sender.addOutgoingFriendRequest(req.getRequestId());
            target.addIncomingFriendRequest(req.getRequestId());
        } catch (RuntimeException e) {
            sender.removeOutgoingFriendRequest(req.getRequestId());
            target.removeIncomingFriendRequest(req.getRequestId());
            throw e;
        }

        notifications.push(target, new FriendRequestNotification(
            IdGenerator.nextNotificationId(),
            target.getUserId(),
            sender.getUsername() + " sent you a friend request.",
            req.getRequestId()));
        return req;
    }

    public void acceptFriendRequest(String requestId) {
        Validator.requireNonBlank(requestId, "requestId");
        User current = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;
        FriendRequest req = ds.findFriendRequestById(requestId)
            .orElseThrow(() -> new InvalidOperationException("Friend request not found."));
        if (!req.getReceiverId().equals(current.getUserId())) {
            throw new AuthorizationException("This request was not sent to you.");
        }
        if (req.getStatus() != FriendRequestStatus.PENDING) {
            throw new InvalidOperationException("Request is no longer pending.");
        }
        req.accept();

        User sender = ds.findUserById(req.getSenderId())
            .orElseThrow(() -> new InvalidOperationException("Sender account no longer exists."));
        try {
            current.addFriend(sender);
            sender.addFriend(current);
        } catch (RuntimeException e) {
            current.removeFriend(sender.getUserId());
            sender.removeFriend(current.getUserId());
            throw e;
        }
        current.removeIncomingFriendRequest(requestId);
        sender.removeOutgoingFriendRequest(requestId);

        notifications.push(sender, new FriendRequestNotification(
            IdGenerator.nextNotificationId(),
            sender.getUserId(),
            current.getUsername() + " accepted your friend request.",
            req.getRequestId()));
    }

    public void rejectFriendRequest(String requestId) {
        Validator.requireNonBlank(requestId, "requestId");
        User current = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;
        FriendRequest req = ds.findFriendRequestById(requestId)
            .orElseThrow(() -> new InvalidOperationException("Friend request not found."));
        if (!req.getReceiverId().equals(current.getUserId())) {
            throw new AuthorizationException("This request was not sent to you.");
        }
        if (req.getStatus() != FriendRequestStatus.PENDING) {
            throw new InvalidOperationException("Request is no longer pending.");
        }
        req.reject();
        current.removeIncomingFriendRequest(requestId);
        ds.findUserById(req.getSenderId())
            .ifPresent(sender -> sender.removeOutgoingFriendRequest(requestId));
    }

    public List<User> listFriends() {
        User current = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;
        List<User> result = new ArrayList<>();
        for (String fid : current.getFriendIds()) {
            ds.findUserById(fid).ifPresent(result::add);
        }
        result.sort(Comparator.comparing(
            u -> u.getUsername() == null ? "" : u.getUsername().toLowerCase()));
        return result;
    }

    public void cancelSentRequest(String requestId) {
        Validator.requireNonBlank(requestId, "requestId");
        User current = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;
        FriendRequest req = ds.findFriendRequestById(requestId)
            .orElseThrow(() -> new InvalidOperationException("Friend request not found."));
        if (!req.getSenderId().equals(current.getUserId())) {
            throw new AuthorizationException("This request was not sent by you.");
        }
        if (req.getStatus() != FriendRequestStatus.PENDING) {
            throw new InvalidOperationException("Request is no longer pending.");
        }
        req.withdraw();
        current.removeOutgoingFriendRequest(requestId);
        ds.findUserById(req.getReceiverId())
            .ifPresent(receiver -> receiver.removeIncomingFriendRequest(requestId));
    }


    public void removeFriend(String username) {
        Validator.requireNonBlank(username, "Username");
        User current = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;
        User target = ds.findUserByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("No user named '" + username + "'."));
        if (!current.isFriendWith(target.getUserId())) {
            throw new InvalidOperationException("You are not friends with " + username + ".");
        }
        current.removeFriend(target.getUserId());
        target.removeFriend(current.getUserId());
    }

    public List<FriendRequest> listIncomingRequests() {
        User current = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;
        List<FriendRequest> result = new ArrayList<>();
        for (String rid : current.getIncomingFriendRequestIds()) {
            ds.findFriendRequestById(rid)
                .filter(r -> r.getStatus() == FriendRequestStatus.PENDING)
                .ifPresent(result::add);
        }
        return result;
    }

    private User requireLoggedIn() {
        User u = DataStore.INSTANCE.getCurrentUser();
        if (u == null) throw new UnauthorizedException("Login required.");
        return u;
    }
}
