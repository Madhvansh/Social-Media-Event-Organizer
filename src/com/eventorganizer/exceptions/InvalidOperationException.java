package com.eventorganizer.exceptions;

public class InvalidOperationException extends AppException {
    public InvalidOperationException(String message) {
        super(message, ErrorCode.ERR_INVALID_OPERATION);
    }

    public InvalidOperationException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
