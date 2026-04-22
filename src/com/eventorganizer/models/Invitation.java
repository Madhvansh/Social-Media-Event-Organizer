package com.eventorganizer.models;

import com.eventorganizer.exceptions.ErrorCode;
import com.eventorganizer.exceptions.InvalidOperationException;
import com.eventorganizer.models.enums.EventStatus;
import com.eventorganizer.models.enums.RSVPStatus;
import com.eventorganizer.store.DataStore;

import java.time.LocalDateTime;
import java.util.Objects;

public class Invitation {
    private final String invitationId;
    private final String eventId;
    private final String inviteeId;
    private RSVPStatus status;
    private final LocalDateTime sentAt;
    private LocalDateTime respondedAt;
    private LocalDateTime updatedAt;

    public Invitation(String invitationId, String eventId, String inviteeId) {
        this.invitationId = Objects.requireNonNull(invitationId, "invitationId");
        this.eventId = Objects.requireNonNull(eventId, "eventId");
        this.inviteeId = Objects.requireNonNull(inviteeId, "inviteeId");
        this.status = RSVPStatus.PENDING;
        this.sentAt = LocalDateTime.now(DataStore.INSTANCE.getClock());
        this.respondedAt = null;
        this.updatedAt = null;
    }

    public String getInvitationId()         { return invitationId; }
    public String getEventId()              { return eventId; }
    public String getInviteeId()            { return inviteeId; }
    public RSVPStatus getStatus()           { return status; }
    public LocalDateTime getSentAt()        { return sentAt; }
    public LocalDateTime getRespondedAt()   { return respondedAt; }
    public LocalDateTime getUpdatedAt()     { return updatedAt; }

    public void respond(RSVPStatus newStatus) {
        if (newStatus == null || newStatus == RSVPStatus.PENDING) {
            throw new IllegalArgumentException("Response must be ACCEPTED, DECLINED, or MAYBE");
        }
        this.status = newStatus;
        LocalDateTime now = LocalDateTime.now(DataStore.INSTANCE.getClock());
        if (this.respondedAt == null) this.respondedAt = now;
        this.updatedAt = now;
    }

    /** Responds while verifying the owning event is still eligible. */
    public void respond(RSVPStatus newStatus, Event event) {
        if (event == null) throw new IllegalArgumentException("event is required");
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new InvalidOperationException(
                "This event has been cancelled.", ErrorCode.ERR_INVALID_OPERATION);
        }
        if (event.isPast()) {
            throw new InvalidOperationException(
                "This event has already taken place.", ErrorCode.ERR_INVALID_OPERATION);
        }
        respond(newStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invitation)) return false;
        Invitation that = (Invitation) o;
        return eventId.equals(that.eventId) && inviteeId.equals(that.inviteeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, inviteeId);
    }

    @Override
    public String toString() {
        return "Invitation{id=" + invitationId + ", event=" + eventId
            + ", invitee=" + inviteeId + ", status=" + status + "}";
    }
}
