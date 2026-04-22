package com.eventorganizer.models;

public class FriendRequestNotification extends Notification {
    private final String requestId;

    public FriendRequestNotification(String notificationId, String recipientId, String message, String requestId) {
        super(notificationId, recipientId, message);
        this.requestId = requestId;
    }

    public String getRequestId() { return requestId; }

    @Override
    public String getCategory() { return "FRIEND REQUEST"; }
}
