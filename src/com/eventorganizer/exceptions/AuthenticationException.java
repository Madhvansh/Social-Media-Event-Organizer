package com.eventorganizer.exceptions;

public class AuthenticationException extends AppException {
    public AuthenticationException(String message) {
        super(message, ErrorCode.ERR_AUTH_INVALID_CREDENTIALS);
    }

    public AuthenticationException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
