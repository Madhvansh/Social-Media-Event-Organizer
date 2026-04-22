package com.eventorganizer.models.enums;

import com.eventorganizer.exceptions.ErrorCode;
import com.eventorganizer.exceptions.ValidationException;

public enum EventStatus {
    ACTIVE,
    CANCELLED;

    public static EventStatus parse(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new ValidationException(
                "Event status is required: Active or Cancelled", ErrorCode.ERR_VALIDATION);
        }
        try {
            return valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                "Invalid event status: must be Active or Cancelled", ErrorCode.ERR_VALIDATION);
        }
    }
}
