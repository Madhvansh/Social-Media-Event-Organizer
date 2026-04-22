package com.eventorganizer.exceptions;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String message) {
        super(message, ErrorCode.ERR_USER_NOT_FOUND);
    }
}
