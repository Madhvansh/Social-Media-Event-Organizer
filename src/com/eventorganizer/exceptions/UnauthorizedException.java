package com.eventorganizer.exceptions;

/**
 * @deprecated use {@link AuthorizationException}. Retained as a subclass so existing
 * catch blocks keep working during the Phase 1 migration.
 */
@Deprecated
public class UnauthorizedException extends AuthorizationException {
    @Deprecated
    public UnauthorizedException(String message) {
        super(message);
    }
}
