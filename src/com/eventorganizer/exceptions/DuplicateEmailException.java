package com.eventorganizer.exceptions;

public class DuplicateEmailException extends ConflictException {
    public DuplicateEmailException(String message) {
        super(message, ErrorCode.ERR_AUTH_DUPLICATE_EMAIL);
    }
}
