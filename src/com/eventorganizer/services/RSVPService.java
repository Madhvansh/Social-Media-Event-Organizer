package com.eventorganizer.services;

import com.eventorganizer.exceptions.AuthorizationException;
import com.eventorganizer.exceptions.EventNotFoundException;
import com.eventorganizer.exceptions.InvalidOperationException;
import com.eventorganizer.exceptions.UnauthorizedException;
import com.eventorganizer.models.Event;
import com.eventorganizer.models.Invitation;
import com.eventorganizer.models.RSVPNotification;
import com.eventorganizer.models.User;
import com.eventorganizer.models.enums.RSVPStatus;
import com.eventorganizer.store.DataStore;
import com.eventorganizer.utils.IdGenerator;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class RSVPService {

    private final NotificationService notifications = new NotificationService();

    public void respond(String eventId, RSVPStatus newStatus) {
        User current = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;
        Event event = ds.findEventById(eventId)
            .orElseThrow(() -> new EventNotFoundException("No event with id '" + eventId + "'."));

        Invitation inv = event.getInvitationForUser(current.getUserId());
        if (inv == null) throw new InvalidOperationException("You have no invitation to this event.");

        inv.respond(newStatus, event);

        ds.findUserById(event.getCreatorId())
            .filter(creator -> !creator.getUserId().equals(current.getUserId()))
            .ifPresent(creator -> notifications.push(creator, new RSVPNotification(
                IdGenerator.nextNotificationId(),
                creator.getUserId(),
                current.getUsername() + " responded " + newStatus + " to '" + event.getName() + "'.",
                event.getEventId(),
                current.getUserId())));
    }

    public List<Invitation> getInvitationsFor(String eventId) {
        User current = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;
        Event event = ds.findEventById(eventId)
            .orElseThrow(() -> new EventNotFoundException("No event with id '" + eventId + "'."));
        if (!event.getCreatorId().equals(current.getUserId())) {
            throw new AuthorizationException("Only the event creator may view invitees.");
        }
        return new ArrayList<>(event.getInvitations());
    }

    public Map<RSVPStatus, Long> viewRSVPSummary(String eventId) {
        User current = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;
        Event event = ds.findEventById(eventId)
            .orElseThrow(() -> new EventNotFoundException("No event with id '" + eventId + "'."));
        if (!event.getCreatorId().equals(current.getUserId())) {
            throw new UnauthorizedException("Only the event creator may view RSVP summary.");
        }
        Map<RSVPStatus, Long> counts = new EnumMap<>(RSVPStatus.class);
        for (RSVPStatus s : RSVPStatus.values()) counts.put(s, 0L);
        for (Invitation inv : event.getInvitations()) {
            counts.merge(inv.getStatus(), 1L, Long::sum);
        }
        return counts;
    }

    private User requireLoggedIn() {
        User u = DataStore.INSTANCE.getCurrentUser();
        if (u == null) throw new UnauthorizedException("Login required.");
        return u;
    }
}
