package com.eventorganizer.utils;

import java.util.concurrent.atomic.AtomicInteger;

public final class IdGenerator {
    private static final AtomicInteger userCounter         = new AtomicInteger(1);
    private static final AtomicInteger eventCounter        = new AtomicInteger(1);
    private static final AtomicInteger notificationCounter = new AtomicInteger(1);
    private static final AtomicInteger invitationCounter   = new AtomicInteger(1);
    private static final AtomicInteger friendReqCounter    = new AtomicInteger(1);

    private IdGenerator() {}

    public static String nextUserId()          { return "U" + userCounter.getAndIncrement(); }
    public static String nextEventId()         { return "E" + eventCounter.getAndIncrement(); }
    public static String nextNotificationId()  { return "N" + notificationCounter.getAndIncrement(); }
    public static String nextInvitationId()    { return "I" + invitationCounter.getAndIncrement(); }
    public static String nextFriendRequestId() { return "F" + friendReqCounter.getAndIncrement(); }

    /** Package-private reset for the JUnit harness. Production code must not call this. */
    public static void resetForTests() {
        userCounter.set(1);
        eventCounter.set(1);
        notificationCounter.set(1);
        invitationCounter.set(1);
        friendReqCounter.set(1);
    }
}
