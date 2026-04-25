package com.eventorganizer.models;

import com.eventorganizer.models.enums.FriendRequestStatus;
import com.eventorganizer.store.DataStore;

import java.time.LocalDateTime;

public class FriendRequest implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private final String requestId;
    private final String senderId;
    private final String receiverId;
    private FriendRequestStatus status;
    private final LocalDateTime sentAt;
    private LocalDateTime resolvedAt;

    public FriendRequest(String requestId, String senderId, String receiverId) {
        this.requestId = requestId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = FriendRequestStatus.PENDING;
        this.sentAt = LocalDateTime.now(DataStore.INSTANCE.getClock());
        this.resolvedAt = null;
    }

    public String getRequestId()              { return requestId; }
    public String getSenderId()               { return senderId; }
    public String getReceiverId()             { return receiverId; }
    public FriendRequestStatus getStatus()    { return status; }
    public LocalDateTime getSentAt()          { return sentAt; }
    public LocalDateTime getResolvedAt()      { return resolvedAt; }

    public void accept() {
        if (status != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("Request is no longer pending");
        }
        this.status = FriendRequestStatus.ACCEPTED;
        this.resolvedAt = LocalDateTime.now(DataStore.INSTANCE.getClock());
    }

    public void reject() {
        if (status != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("Request is no longer pending");
        }
        this.status = FriendRequestStatus.REJECTED;
        this.resolvedAt = LocalDateTime.now(DataStore.INSTANCE.getClock());
    }

    public void withdraw() {
        if (status != FriendRequestStatus.PENDING) {
            throw new IllegalStateException("Request is no longer pending");
        }
        this.status = FriendRequestStatus.WITHDRAWN;
        this.resolvedAt = LocalDateTime.now(DataStore.INSTANCE.getClock());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendRequest)) return false;
        return requestId.equals(((FriendRequest) o).requestId);
    }

    @Override
    public int hashCode() {
        return requestId.hashCode();
    }

    @Override
    public String toString() {
        return "FriendRequest{id=" + requestId + ", from=" + senderId
            + ", to=" + receiverId + ", status=" + status + "}";
    }
}
