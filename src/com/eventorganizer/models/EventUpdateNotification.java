package com.eventorganizer.models;

public class EventUpdateNotification extends Notification {
    private final String eventId;

    public EventUpdateNotification(String notificationId, String recipientId, String message, String eventId) {
        super(notificationId, recipientId, message);
        this.eventId = eventId;
    }

    public String getEventId() { return eventId; }

    @Override
    public String getCategory() { return "EVENT UPDATE"; }
}
