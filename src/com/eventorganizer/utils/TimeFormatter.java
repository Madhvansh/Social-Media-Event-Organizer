package com.eventorganizer.utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.eventorganizer.store.DataStore;

public final class TimeFormatter {
    private static final DateTimeFormatter ABSOLUTE =
        DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a");
    private static final DateTimeFormatter MONTH_DAY =
        DateTimeFormatter.ofPattern("MMM d");
    private static final DateTimeFormatter MONTH_DAY_YEAR =
        DateTimeFormatter.ofPattern("MMM d, yyyy");

    private TimeFormatter() {}

    public static String absolute(LocalDateTime dt) {
        return dt == null ? "-" : dt.format(ABSOLUTE);
    }

    public static String relative(LocalDateTime dt) {
        if (dt == null) return "-";
        LocalDateTime now = LocalDateTime.now(DataStore.getClockOrDefault());
        Duration d = Duration.between(dt, now);
        long seconds = d.getSeconds();

        if (seconds < 0) return absolute(dt);
        if (seconds < 60) return "just now";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + "m ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + "h ago";
        long days = hours / 24;
        if (days == 1) return "yesterday";
        if (days < 7) return days + "d ago";

        LocalDate eventDate = dt.toLocalDate();
        LocalDate today = now.toLocalDate();
        if (eventDate.getYear() == today.getYear()) return eventDate.format(MONTH_DAY);
        return eventDate.format(MONTH_DAY_YEAR);
    }
}
