package com.eventorganizer.models.enums;

import com.eventorganizer.exceptions.ErrorCode;
import com.eventorganizer.exceptions.ValidationException;

public enum FriendRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    WITHDRAWN;

    public static FriendRequestStatus parse(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new ValidationException(
                "Friend request status is required: Pending, Accepted, Rejected, or Withdrawn",
                ErrorCode.ERR_VALIDATION);
        }
        try {
            return valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                "Invalid friend request status: must be Pending, Accepted, Rejected, or Withdrawn",
                ErrorCode.ERR_VALIDATION);
        }
    }
}
