package com.eventorganizer.models;

import com.eventorganizer.exceptions.ErrorCode;
import com.eventorganizer.exceptions.ValidationException;
import com.eventorganizer.interfaces.Reportable;
import com.eventorganizer.models.enums.EventStatus;
import com.eventorganizer.models.enums.EventType;
import com.eventorganizer.models.enums.RSVPStatus;
import com.eventorganizer.store.DataStore;
import com.eventorganizer.utils.DateUtil;
import com.eventorganizer.utils.Limits;
import com.eventorganizer.utils.Validator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Event implements Reportable, java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final String eventId;
    private String name;
    private String description;
    private String location;
    private LocalDateTime dateTime;
    private final String creatorId;
    private EventStatus status;
    private final List<Invitation> invitations;

    protected Event(String eventId, String name, String description, LocalDateTime dateTime,
                    String location, String creatorId) {
        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.dateTime = dateTime;
        this.location = location;
        this.creatorId = creatorId;
        this.status = EventStatus.ACTIVE;
        this.invitations = Collections.synchronizedList(new ArrayList<>());
    }

    public String getEventId()          { return eventId; }
    public String getName()             { return name; }
    public String getDescription()      { return description; }
    public String getLocation()         { return location; }
    public LocalDateTime getDateTime()  { return dateTime; }
    public String getCreatorId()        { return creatorId; }
    public EventStatus getStatus()      { return status; }

    /**
     * Sets the event name. Non-null, non-blank, capped at {@link Limits#EVENT_NAME_MAX}.
     * Services are expected to pre-validate and trim; this setter is the entity-level
     * invariant guard (belt-and-braces).
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Event name is required.", ErrorCode.ERR_VALIDATION);
        }
        Validator.requireLength(name, Limits.EVENT_NAME_MAX, "Event name");
        this.name = name;
    }

    /** Sets the description. Null is coerced to "". Capped at {@link Limits#EVENT_DESC_MAX}. */
    public void setDescription(String description) {
        String v = description == null ? "" : description;
        Validator.requireLength(v, Limits.EVENT_DESC_MAX, "Event description");
        this.description = v;
    }

    /**
     * Sets the location. Non-null, non-blank, capped at {@link Limits#LOCATION_MAX}.
     */
    public void setLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new ValidationException("Event location is required.", ErrorCode.ERR_VALIDATION);
        }
        Validator.requireLength(location, Limits.LOCATION_MAX, "Event location");
        this.location = location;
    }

    /**
     * Sets the event date/time. Rejects null and dates beyond {@link Limits#FAR_FUTURE_YEARS}.
     * Past dates are permitted so {@link com.eventorganizer.services.EventService#editEvent}
     * (which pre-validates future-dates) and test fixtures still compose; callers
     * wanting to forbid past dates must check beforehand.
     */
    public void setDateTime(LocalDateTime dt) {
        if (dt == null) {
            throw new ValidationException("Event date/time is required.", ErrorCode.ERR_VALIDATION);
        }
        LocalDateTime cap = LocalDateTime.now(DataStore.INSTANCE.getClock())
            .plusYears(Limits.FAR_FUTURE_YEARS);
        if (dt.isAfter(cap)) {
            throw new ValidationException(
                "Event date/time cannot be more than " + Limits.FAR_FUTURE_YEARS + " years in the future.",
                ErrorCode.ERR_VALIDATION);
        }
        this.dateTime = dt;
    }

    public abstract EventType getType();

    public abstract boolean canInvite(User creator, User invitee);

    public void addInvitation(Invitation inv) {
        invitations.add(inv);
    }

    public boolean removeInvitation(Invitation inv) {
        return invitations.remove(inv);
    }

    public Invitation getInvitationForUser(String userId) {
        synchronized (invitations) {
            for (Invitation inv : invitations) {
                if (inv.getInviteeId().equals(userId)) return inv;
            }
        }
        return null;
    }

    public boolean hasInvited(String userId) {
        return getInvitationForUser(userId) != null;
    }

    /**
     * Returns a snapshot of invitations. Safe to iterate without external synchronization
     * because the backing list is a synchronized wrapper and the copy is taken atomically.
     */
    public List<Invitation> getInvitations() {
        synchronized (invitations) {
            return new ArrayList<>(invitations);
        }
    }

    public boolean isPast() {
        return dateTime.isBefore(LocalDateTime.now(DataStore.INSTANCE.getClock()));
    }

    public boolean isUpcoming() {
        return !isPast() && status == EventStatus.ACTIVE;
    }

    public void cancel() {
        this.status = EventStatus.CANCELLED;
    }

    public long countByStatus(RSVPStatus s) {
        synchronized (invitations) {
            return invitations.stream().filter(i -> i.getStatus() == s).count();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;
        return eventId.equals(((Event) o).eventId);
    }

    @Override
    public int hashCode() {
        return eventId.hashCode();
    }

    @Override
    public String toString() {
        return "Event{id=" + eventId + ", name=" + name + ", type=" + getType() + ", status=" + status + "}";
    }

    @Override
    public String generateSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Event: ").append(name).append(" [").append(getType()).append("]\n");
        sb.append("  ID:          ").append(eventId).append("\n");
        sb.append("  Status:      ").append(status).append("\n");
        sb.append("  When:        ").append(DateUtil.format(dateTime)).append("\n");
        sb.append("  Where:       ").append(location).append("\n");
        sb.append("  Description: ").append(description).append("\n");
        sb.append("  Invitees:    ").append(invitations.size()).append("\n");
        sb.append("    Accepted:  ").append(countByStatus(RSVPStatus.ACCEPTED)).append("\n");
        sb.append("    Declined:  ").append(countByStatus(RSVPStatus.DECLINED)).append("\n");
        sb.append("    Maybe:     ").append(countByStatus(RSVPStatus.MAYBE)).append("\n");
        sb.append("    Pending:   ").append(countByStatus(RSVPStatus.PENDING)).append("\n");
        return sb.toString();
    }
}
