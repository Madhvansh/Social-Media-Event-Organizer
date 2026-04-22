package com.eventorganizer.models.enums;

import com.eventorganizer.exceptions.ErrorCode;
import com.eventorganizer.exceptions.ValidationException;

public enum RSVPStatus {
    ACCEPTED,
    DECLINED,
    MAYBE,
    PENDING;

    public static RSVPStatus parse(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new ValidationException(
                "RSVP status is required: Accepted, Declined, or Maybe", ErrorCode.ERR_VALIDATION);
        }
        try {
            return valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                "Invalid status: must be Accepted, Declined, or Maybe", ErrorCode.ERR_VALIDATION);
        }
    }
}
