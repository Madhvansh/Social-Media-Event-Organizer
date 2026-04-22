package com.eventorganizer.exceptions;

public class ValidationException extends AppException {
    public ValidationException(String message) {
        super(message, ErrorCode.ERR_VALIDATION);
    }

    public ValidationException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
