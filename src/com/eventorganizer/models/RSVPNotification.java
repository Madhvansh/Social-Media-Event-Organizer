package com.eventorganizer.models;

public class RSVPNotification extends Notification {
    private final String eventId;
    private final String responderId;

    public RSVPNotification(String notificationId, String recipientId, String message, String eventId, String responderId) {
        super(notificationId, recipientId, message);
        this.eventId = eventId;
        this.responderId = responderId;
    }

    public String getEventId()     { return eventId; }
    public String getResponderId() { return responderId; }

    @Override
    public String getCategory() { return "RSVP"; }
}
