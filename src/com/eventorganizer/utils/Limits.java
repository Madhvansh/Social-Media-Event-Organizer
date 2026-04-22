package com.eventorganizer.utils;

public final class Limits {
    private Limits() {}

    public static final int USERNAME_MAX = 32;
    public static final int EMAIL_MAX = 254;
    public static final int PASSWORD_MIN = 6;
    public static final int PASSWORD_MAX = 128;
    public static final int BIO_MAX = 500;
    public static final int EVENT_NAME_MAX = 100;
    public static final int EVENT_DESC_MAX = 1000;
    public static final int LOCATION_MAX = 200;
    public static final int NOTIFICATIONS_PER_USER_MAX = 500;
    public static final int FAR_FUTURE_YEARS = 10;
    public static final int EDIT_COALESCE_SECONDS = 60;
    public static final int FRIEND_REQUEST_COOLDOWN_HOURS = 24;
    public static final int EVENT_CONFLICT_WINDOW_MINUTES = 60;
}
