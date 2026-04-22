package com.eventorganizer.exceptions;

public class DuplicateInvitationException extends ConflictException {
    public DuplicateInvitationException(String message) {
        super(message, ErrorCode.ERR_CONFLICT_INVITATION);
    }
}
