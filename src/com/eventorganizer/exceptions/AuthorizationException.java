package com.eventorganizer.exceptions;

public class AuthorizationException extends AppException {
    public AuthorizationException(String message) {
        super(message, ErrorCode.ERR_UNAUTHORIZED);
    }

    public AuthorizationException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
