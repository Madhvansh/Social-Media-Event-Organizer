package com.eventorganizer.models;

import com.eventorganizer.store.DataStore;
import com.eventorganizer.utils.DateUtil;

import java.time.LocalDateTime;

public abstract class Notification implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final String notificationId;
    private final String recipientId;
    private final String message;
    private final LocalDateTime timestamp;
    private boolean read;

    protected Notification(String notificationId, String recipientId, String message) {
        this.notificationId = notificationId;
        this.recipientId = recipientId;
        this.message = message;
        this.timestamp = LocalDateTime.now(DataStore.INSTANCE.getClock());
        this.read = false;
    }

    public String getNotificationId() { return notificationId; }
    public String getRecipientId()    { return recipientId; }
    public String getMessage()        { return message; }
    public LocalDateTime getTimestamp(){ return timestamp; }
    public boolean isRead()           { return read; }

    public void markAsRead() { this.read = true; }

    public abstract String getCategory();

    public String display() {
        return String.format("[%s] [%s] %s%s",
            getCategory(),
            DateUtil.format(timestamp),
            message,
            read ? "" : "  (new)");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        return notificationId.equals(((Notification) o).notificationId);
    }

    @Override
    public int hashCode() {
        return notificationId.hashCode();
    }

    @Override
    public String toString() {
        return "Notification{id=" + notificationId + ", category=" + getCategory()
            + ", read=" + read + "}";
    }
}
