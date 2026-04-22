package com.eventorganizer.exceptions;

public class DuplicateUsernameException extends ConflictException {
    public DuplicateUsernameException(String message) {
        super(message, ErrorCode.ERR_AUTH_DUPLICATE_USERNAME);
    }
}
