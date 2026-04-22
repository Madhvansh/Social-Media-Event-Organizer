package com.eventorganizer.exceptions;

public class DuplicateFriendRequestException extends ConflictException {
    public DuplicateFriendRequestException(String message) {
        super(message, ErrorCode.ERR_CONFLICT_FRIEND_REQUEST);
    }
}
