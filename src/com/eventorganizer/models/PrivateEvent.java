package com.eventorganizer.models;

import com.eventorganizer.models.enums.EventType;

import java.time.LocalDateTime;

public class PrivateEvent extends Event {
    public PrivateEvent(String eventId, String name, String description, LocalDateTime dateTime,
                        String location, String creatorId) {
        super(eventId, name, description, dateTime, location, creatorId);
    }

    @Override
    public EventType getType() { return EventType.PRIVATE; }

    @Override
    public boolean canInvite(User creator, User invitee) {
        if (creator == null || invitee == null) return false;
        if (creator.getUserId().equals(invitee.getUserId())) return false;
        return creator.isFriendWith(invitee.getUserId());
    }

    @Override
    public String generateSummary() {
        return super.generateSummary() + "  Note: Private event - only friends of creator may be invited.\n";
    }
}
