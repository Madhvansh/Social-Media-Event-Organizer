package com.eventorganizer.services;

import com.eventorganizer.exceptions.AuthorizationException;
import com.eventorganizer.exceptions.DuplicateInvitationException;
import com.eventorganizer.exceptions.EventNotFoundException;
import com.eventorganizer.exceptions.InvalidOperationException;
import com.eventorganizer.exceptions.UnauthorizedException;
import com.eventorganizer.exceptions.UserNotFoundException;
import com.eventorganizer.models.Event;
import com.eventorganizer.models.EventUpdateNotification;
import com.eventorganizer.models.Invitation;
import com.eventorganizer.models.InvitationNotification;
import com.eventorganizer.models.User;
import com.eventorganizer.models.dto.BatchInviteResult;
import com.eventorganizer.models.enums.EventStatus;
import com.eventorganizer.models.enums.RSVPStatus;
import com.eventorganizer.store.DataStore;
import com.eventorganizer.utils.IdGenerator;
import com.eventorganizer.utils.Limits;
import com.eventorganizer.utils.Validator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InvitationService {

    private final NotificationService notifications = new NotificationService();

    public Invitation inviteFriend(String eventId, String username) {
        Validator.requireNonBlank(eventId, "eventId");
        Validator.requireNonBlank(username, "Username");
        Validator.requireLength(username, Limits.USERNAME_MAX, "Username");
        User creator = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;

        Event event = ds.findEventById(eventId)
            .orElseThrow(() -> new EventNotFoundException("No event with id '" + eventId + "'."));
        if (!event.getCreatorId().equals(creator.getUserId())) {
            throw new AuthorizationException("Only the event creator may send invitations.");
        }
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot invite to a cancelled event.");
        }
        if (event.isPast()) {
            throw new InvalidOperationException("Cannot invite to a past event.");
        }

        User invitee = ds.findUserByUsername(username)
            .orElseThrow(() -> new UserNotFoundException("No user named '" + username + "'."));

        if (invitee.getUserId().equals(creator.getUserId())) {
            throw new InvalidOperationException("You cannot invite yourself to your own event.");
        }

        if (!event.canInvite(creator, invitee)) {
            throw new InvalidOperationException(
                "Cannot invite " + invitee.getUsername()
                + " - private events are limited to the creator's friends.");
        }

        if (event.hasInvited(invitee.getUserId())) {
            throw new DuplicateInvitationException(
                invitee.getUsername() + " has already been invited to this event.");
        }

        Invitation inv = new Invitation(IdGenerator.nextInvitationId(), event.getEventId(), invitee.getUserId());
        event.addInvitation(inv);
        try {
            ds.indexInvitation(inv);
        } catch (RuntimeException e) {
            // Invariant: either both the event list and the index contain `inv`, or neither does.
            event.removeInvitation(inv);
            ds.unindexInvitation(event.getEventId(), invitee.getUserId());
            throw e;
        }

        notifications.push(invitee, new InvitationNotification(
            IdGenerator.nextNotificationId(),
            invitee.getUserId(),
            creator.getUsername() + " invited you to '" + event.getName() + "'.",
            event.getEventId()));
        return inv;
    }

    public Map<String, String> inviteMultiple(String eventId, List<String> usernames) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String u : usernames) {
            try {
                inviteFriend(eventId, u);
                result.put(u, "invited");
            } catch (RuntimeException e) {
                result.put(u, "skipped: " + e.getMessage());
            }
        }
        return result;
    }

    public BatchInviteResult inviteMany(String eventId, List<String> usernames) {
        BatchInviteResult result = new BatchInviteResult();
        if (usernames == null) return result;
        for (String u : usernames) {
            try {
                inviteFriend(eventId, u);
                result.addInvited(u);
            } catch (RuntimeException e) {
                result.addFailure(u, e.getMessage());
            }
        }
        return result;
    }

    public void revoke(String eventId, String inviteeId) {
        Validator.requireNonBlank(eventId, "eventId");
        Validator.requireNonBlank(inviteeId, "inviteeId");
        User creator = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;
        Event event = ds.findEventById(eventId)
            .orElseThrow(() -> new EventNotFoundException("No event with id '" + eventId + "'."));
        if (!event.getCreatorId().equals(creator.getUserId())) {
            throw new AuthorizationException("Only the event creator may revoke invitations.");
        }
        Invitation inv = event.getInvitationForUser(inviteeId);
        if (inv == null) {
            throw new InvalidOperationException("No invitation to revoke for that user.");
        }
        if (inv.getStatus() != RSVPStatus.PENDING) {
            throw new InvalidOperationException(
                "Cannot revoke an invitation that has already been responded to.");
        }
        event.removeInvitation(inv);
        ds.unindexInvitation(event.getEventId(), inviteeId);

        ds.findUserById(inviteeId).ifPresent(u ->
            notifications.push(u, new EventUpdateNotification(
                IdGenerator.nextNotificationId(),
                u.getUserId(),
                "Your invitation to '" + event.getName() + "' has been revoked.",
                event.getEventId())));
    }

    public List<Invitation> viewInvitees(String eventId) {
        Validator.requireNonBlank(eventId, "eventId");
        User creator = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;
        Event event = ds.findEventById(eventId)
            .orElseThrow(() -> new EventNotFoundException("No event with id '" + eventId + "'."));
        if (!event.getCreatorId().equals(creator.getUserId())) {
            throw new AuthorizationException("Only the event creator may view invitees.");
        }
        return new ArrayList<>(event.getInvitations());
    }

    public List<Event> eventsWithPendingInvitationsFor(User user) {
        DataStore ds = DataStore.INSTANCE;
        List<Event> result = new ArrayList<>();
        for (Event e : ds.getAllEvents()) {
            Invitation inv = e.getInvitationForUser(user.getUserId());
            if (inv != null
                && inv.getStatus() == com.eventorganizer.models.enums.RSVPStatus.PENDING
                && e.getStatus() == EventStatus.ACTIVE) {
                result.add(e);
            }
        }
        return result;
    }

    /* Self-invite to a public event. */
    public Invitation joinPublicEvent(String eventId) {
        Validator.requireNonBlank(eventId, "eventId");
        User user = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;
        Event event = ds.findEventById(eventId)
            .orElseThrow(() -> new EventNotFoundException("No event with id '" + eventId + "'."));
        if (event.getCreatorId().equals(user.getUserId())) {
            throw new InvalidOperationException("You're the creator — no need to RSVP.");
        }
        if (!(event instanceof com.eventorganizer.models.PublicEvent)) {
            throw new InvalidOperationException(
                "You can only self-join public events. Private events require an invitation.");
        }
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new InvalidOperationException("This event has been cancelled.");
        }
        if (event.isPast()) {
            throw new InvalidOperationException("This event is in the past.");
        }
        if (event.hasInvited(user.getUserId())) {
            return event.getInvitationForUser(user.getUserId());
        }
        Invitation inv = new Invitation(
            IdGenerator.nextInvitationId(), event.getEventId(), user.getUserId());
        event.addInvitation(inv);
        try {
            ds.indexInvitation(inv);
        } catch (RuntimeException e) {
            event.removeInvitation(inv);
            ds.unindexInvitation(event.getEventId(), user.getUserId());
            throw e;
        }
        
        /* Notify the creator so they know someone joined. */
        
        ds.findUserById(event.getCreatorId()).ifPresent(creator ->
            notifications.push(creator, new InvitationNotification(
                IdGenerator.nextNotificationId(),
                creator.getUserId(),
                user.getUsername() + " joined '" + event.getName() + "'.",
                event.getEventId())));
        return inv;
    }

    private User requireLoggedIn() {
        User u = DataStore.INSTANCE.getCurrentUser();
        if (u == null) throw new UnauthorizedException("Login required.");
        return u;
    }
}
