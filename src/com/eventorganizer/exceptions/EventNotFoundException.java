package com.eventorganizer.exceptions;

public class EventNotFoundException extends NotFoundException {
    public EventNotFoundException(String message) {
        super(message, ErrorCode.ERR_EVENT_NOT_FOUND);
    }
}
