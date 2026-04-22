package com.eventorganizer.exceptions;

public class NotFoundException extends AppException {
    public NotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
