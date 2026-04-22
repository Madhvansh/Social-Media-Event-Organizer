package com.eventorganizer.models;

import com.eventorganizer.models.enums.EventType;

import java.time.LocalDateTime;

public class PublicEvent extends Event {
    public PublicEvent(String eventId, String name, String description, LocalDateTime dateTime,
                       String location, String creatorId) {
        super(eventId, name, description, dateTime, location, creatorId);
    }

    @Override
    public EventType getType() { return EventType.PUBLIC; }

    @Override
    public boolean canInvite(User creator, User invitee) {
        return creator != null && invitee != null && !creator.getUserId().equals(invitee.getUserId());
    }

    @Override
    public String generateSummary() {
        return super.generateSummary() + "  Note: Public event - anyone may be invited.\n";
    }
}
