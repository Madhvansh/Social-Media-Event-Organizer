package com.eventorganizer.exceptions;

public class InvitationNotFoundException extends NotFoundException {
    public InvitationNotFoundException(String message) {
        super(message, ErrorCode.ERR_INVITATION_NOT_FOUND);
    }
}
