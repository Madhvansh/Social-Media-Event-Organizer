package com.eventorganizer.models;

import com.eventorganizer.interfaces.Reportable;
import com.eventorganizer.models.enums.EventStatus;
import com.eventorganizer.models.enums.EventType;
import com.eventorganizer.models.enums.RSVPStatus;
import com.eventorganizer.store.DataStore;
import com.eventorganizer.utils.DateUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Event implements Reportable {
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
        this.invitations = new ArrayList<>();
    }

    public String getEventId()          { return eventId; }
    public String getName()             { return name; }
    public String getDescription()      { return description; }
    public String getLocation()         { return location; }
    public LocalDateTime getDateTime()  { return dateTime; }
    public String getCreatorId()        { return creatorId; }
    public EventStatus getStatus()      { return status; }

    public void setName(String name)               { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setLocation(String location)       { this.location = location; }
    public void setDateTime(LocalDateTime dt)      { this.dateTime = dt; }

    public abstract EventType getType();

    public abstract boolean canInvite(User creator, User invitee);

    public void addInvitation(Invitation inv) {
        invitations.add(inv);
    }

    public boolean removeInvitation(Invitation inv) {
        return invitations.remove(inv);
    }

    public Invitation getInvitationForUser(String userId) {
        for (Invitation inv : invitations) {
            if (inv.getInviteeId().equals(userId)) return inv;
        }
        return null;
    }

    public boolean hasInvited(String userId) {
        return getInvitationForUser(userId) != null;
    }

    public List<Invitation> getInvitations() {
        return Collections.unmodifiableList(invitations);
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
        return invitations.stream().filter(i -> i.getStatus() == s).count();
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
