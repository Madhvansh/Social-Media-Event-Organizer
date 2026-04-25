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

    /** Snapshot the current counter values so persistence can restore them. */
    public static long[] snapshot() {
        return new long[] {
            userCounter.get(), eventCounter.get(), notificationCounter.get(),
            invitationCounter.get(), friendReqCounter.get()
        };
    }

    /** Restore counters from a snapshot. Out-of-range values default to 1. */
    public static void restore(long[] snap) {
        if (snap == null || snap.length < 5) return;
        userCounter.set(safeInt(snap[0]));
        eventCounter.set(safeInt(snap[1]));
        notificationCounter.set(safeInt(snap[2]));
        invitationCounter.set(safeInt(snap[3]));
        friendReqCounter.set(safeInt(snap[4]));
    }

    private static int safeInt(long v) {
        if (v < 1) return 1;
        if (v > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) v;
    }
}
