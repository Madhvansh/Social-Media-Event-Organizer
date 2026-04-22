package com.eventorganizer.models;

public class InvitationNotification extends Notification {
    private final String eventId;

    public InvitationNotification(String notificationId, String recipientId, String message, String eventId) {
        super(notificationId, recipientId, message);
        this.eventId = eventId;
    }

    public String getEventId() { return eventId; }

    @Override
    public String getCategory() { return "INVITATION"; }
}
