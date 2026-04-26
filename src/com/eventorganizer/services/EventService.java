package com.eventorganizer.services;

import com.eventorganizer.exceptions.AuthorizationException;
import com.eventorganizer.exceptions.EventNotFoundException;
import com.eventorganizer.exceptions.InvalidOperationException;
import com.eventorganizer.exceptions.UnauthorizedException;
import com.eventorganizer.models.Event;
import com.eventorganizer.models.EventUpdateNotification;
import com.eventorganizer.models.Invitation;
import com.eventorganizer.models.PrivateEvent;
import com.eventorganizer.models.PublicEvent;
import com.eventorganizer.models.User;
import com.eventorganizer.models.enums.EventStatus;
import com.eventorganizer.models.enums.EventType;
import com.eventorganizer.store.DataStore;
import com.eventorganizer.utils.IdGenerator;
import com.eventorganizer.utils.Limits;
import com.eventorganizer.utils.Validator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EventService {

    public static final Comparator<Event> UPCOMING_ASC_BY_NAME =
        Comparator.comparing(Event::getDateTime)
            .thenComparing(e -> e.getName() == null ? "" : e.getName().toLowerCase());

    public static final Comparator<Event> PAST_DESC_BY_NAME =
        Comparator.comparing(Event::getDateTime).reversed()
            .thenComparing(e -> e.getName() == null ? "" : e.getName().toLowerCase());

    public static final class CreateEventResult {
        private final Event event;
        private final List<String> warnings;
        public CreateEventResult(Event event, List<String> warnings) {
            this.event = event;
            this.warnings = warnings == null ? List.of() : List.copyOf(warnings);
        }
        public Event getEvent()            { return event; }
        public List<String> getWarnings()  { return warnings; }
    }

    private final NotificationService notifications = new NotificationService();

    public Event createEvent(String name, String description, LocalDateTime dateTime,
                             String location, EventType type) {
        return createEventWithWarnings(name, description, dateTime, location, type).getEvent();
    }

    public CreateEventResult createEventWithWarnings(String name, String description,
                                                     LocalDateTime dateTime, String location,
                                                     EventType type) {
        User creator = requireLoggedIn();

        if (!Validator.isNonEmpty(name))     throw new InvalidOperationException("Event name is required.");
        if (!Validator.isNonEmpty(location)) throw new InvalidOperationException("Location is required.");
        Validator.requireLength(name, Limits.EVENT_NAME_MAX, "Event name");
        Validator.requireLength(description, Limits.EVENT_DESC_MAX, "Event description");
        Validator.requireLength(location, Limits.LOCATION_MAX, "Event location");
        if (!Validator.isFutureDate(dateTime)) {
            throw new InvalidOperationException("Event date/time must be in the future.");
        }
        if (isFarFuture(dateTime)) {
            throw new InvalidOperationException(
                "Event date/time cannot be more than " + Limits.FAR_FUTURE_YEARS + " years in the future.");
        }
        if (type == null) throw new InvalidOperationException("Event type is required.");

        List<String> warnings = detectConflicts(creator.getUserId(), dateTime, null);

        String eventId = IdGenerator.nextEventId();
        Event e = (type == EventType.PUBLIC)
            ? new PublicEvent(eventId, name.trim(),
                description == null ? "" : description.trim(),
                dateTime, location.trim(), creator.getUserId())
            : new PrivateEvent(eventId, name.trim(),
                description == null ? "" : description.trim(),
                dateTime, location.trim(), creator.getUserId());

        DataStore.INSTANCE.saveEvent(e);
        return new CreateEventResult(e, warnings);
    }

    public Event editEvent(String eventId, String newName, String newDescription,
                           LocalDateTime newDateTime, String newLocation) {
        Validator.requireNonBlank(eventId, "eventId");
        User current = requireLoggedIn();
        Event e = requireEvent(eventId);
        if (!e.getCreatorId().equals(current.getUserId())) {
            throw new AuthorizationException("Only the event creator may edit this event.");
        }
        if (e.getStatus() == EventStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot edit a cancelled event.");
        }
        if (e.isPast()) {
            throw new InvalidOperationException("Cannot edit a past event.");
        }

        if (newName != null && Validator.isNonEmpty(newName)) {
            Validator.requireLength(newName, Limits.EVENT_NAME_MAX, "Event name");
            e.setName(newName.trim());
        }
        if (newDescription != null) {
            Validator.requireLength(newDescription, Limits.EVENT_DESC_MAX, "Event description");
            e.setDescription(newDescription.trim());
        }
        if (newLocation != null && Validator.isNonEmpty(newLocation)) {
            Validator.requireLength(newLocation, Limits.LOCATION_MAX, "Event location");
            e.setLocation(newLocation.trim());
        }
        if (newDateTime != null) {
            if (!Validator.isFutureDate(newDateTime)) {
                throw new InvalidOperationException("New event date/time must be in the future.");
            }
            if (isFarFuture(newDateTime)) {
                throw new InvalidOperationException(
                    "Event date/time cannot be more than " + Limits.FAR_FUTURE_YEARS + " years in the future.");
            }
            e.setDateTime(newDateTime);
        }

        notifyInvitees(e, "Event '" + e.getName() + "' has been updated.");
        return e;
    }

    /** Cancels an active event. If it's already cancelled, just returns. */
    public void cancelEvent(String eventId) {
        cancelEvent(eventId, null);
    }

    /**
     * Cancels an active event, adding an optional reason to the notification message.
     * If it's already cancelled, just returns.
     */
    public void cancelEvent(String eventId, String reason) {
        Validator.requireNonBlank(eventId, "eventId");
        User current = requireLoggedIn();
        Event e = requireEvent(eventId);
        if (!e.getCreatorId().equals(current.getUserId())) {
            throw new AuthorizationException("Only the event creator may cancel this event.");
        }
        if (e.getStatus() == EventStatus.CANCELLED) {
            return;
        }
        e.cancel();
        String msg = "Event '" + e.getName() + "' has been cancelled.";
        if (reason != null && !reason.trim().isEmpty()) {
            msg = msg + " Reason: " + reason.trim();
        }
        notifyInvitees(e, msg);
    }

    public Event viewEventDetails(String eventId) {
        Validator.requireNonBlank(eventId, "eventId");
        return requireEvent(eventId);
    }

    public List<Event> viewMyEvents() {
        User current = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;
        List<Event> mine = new ArrayList<>();
        for (String eid : ds.eventIdsForCreator(current.getUserId())) {
            ds.findEventById(eid).ifPresent(mine::add);
        }
        mine.sort(UPCOMING_ASC_BY_NAME);
        return mine;
    }

    public List<Event> viewUpcoming() {
        User current = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;
        List<Event> list = new ArrayList<>();
        for (String eid : ds.eventIdsForCreator(current.getUserId())) {
            ds.findEventById(eid).filter(Event::isUpcoming).ifPresent(list::add);
        }
        list.sort(UPCOMING_ASC_BY_NAME);
        return list;
    }

    public List<Event> viewPast() {
        User current = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;
        List<Event> list = new ArrayList<>();
        for (String eid : ds.eventIdsForCreator(current.getUserId())) {
            ds.findEventById(eid).filter(Event::isPast).ifPresent(list::add);
        }
        list.sort(PAST_DESC_BY_NAME);
        return list;
    }

    /** Returns all active upcoming public events not created by the current user. */
    public List<Event> discoverPublicEvents() {
        User current = requireLoggedIn();
        DataStore ds = DataStore.INSTANCE;
        List<Event> list = new ArrayList<>();
        for (Event e : ds.getAllEvents()) {
            if (!(e instanceof PublicEvent)) continue;
            if (e.getCreatorId().equals(current.getUserId())) continue;
            if (e.getStatus() == EventStatus.CANCELLED) continue;
            if (e.isPast()) continue;
            list.add(e);
        }
        list.sort(UPCOMING_ASC_BY_NAME);
        return list;
    }

    private boolean isFarFuture(LocalDateTime dt) {
        if (dt == null) return false;
        LocalDateTime cap = LocalDateTime.now(DataStore.INSTANCE.getClock())
            .plusYears(Limits.FAR_FUTURE_YEARS);
        return dt.isAfter(cap);
    }

    private List<String> detectConflicts(String creatorId, LocalDateTime target, String excludeEventId) {
        List<String> warnings = new ArrayList<>();
        long windowMin = Limits.EVENT_CONFLICT_WINDOW_MINUTES;
        for (Event other : DataStore.INSTANCE.getAllEvents()) {
            if (!other.getCreatorId().equals(creatorId)) continue;
            if (other.getStatus() == EventStatus.CANCELLED) continue;
            if (excludeEventId != null && excludeEventId.equals(other.getEventId())) continue;
            long deltaMin = Math.abs(Duration.between(target, other.getDateTime()).toMinutes());
            if (deltaMin <= windowMin) {
                warnings.add("Heads up - you have another event ('" + other.getName()
                    + "') starting within " + windowMin + " minutes of this time.");
            }
        }
        return warnings;
    }

    private void notifyInvitees(Event e, String message) {
        DataStore ds = DataStore.INSTANCE;
        for (Invitation inv : e.getInvitations()) {
            ds.findUserById(inv.getInviteeId()).ifPresent(u ->
                notifications.pushCoalesced(u, new EventUpdateNotification(
                    IdGenerator.nextNotificationId(),
                    u.getUserId(),
                    message,
                    e.getEventId())));
        }
    }

    private Event requireEvent(String eventId) {
        return DataStore.INSTANCE.findEventById(eventId)
            .orElseThrow(() -> new EventNotFoundException("No event with id '" + eventId + "'."));
    }

    private User requireLoggedIn() {
        User u = DataStore.INSTANCE.getCurrentUser();
        if (u == null) throw new UnauthorizedException("Login required.");
        return u;
    }
}
