package com.eventorganizer.models.enums;

import com.eventorganizer.exceptions.ErrorCode;
import com.eventorganizer.exceptions.ValidationException;

public enum EventType {
    PUBLIC,
    PRIVATE;

    public static EventType parse(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new ValidationException(
                "Event type is required: Public or Private", ErrorCode.ERR_VALIDATION);
        }
        try {
            return valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException(
                "Invalid event type: must be Public or Private", ErrorCode.ERR_VALIDATION);
        }
    }
}
