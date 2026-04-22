package com.eventorganizer.exceptions;

public class ConflictException extends AppException {
    public ConflictException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
