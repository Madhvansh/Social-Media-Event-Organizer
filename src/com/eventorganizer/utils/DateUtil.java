package com.eventorganizer.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

import com.eventorganizer.exceptions.ErrorCode;
import com.eventorganizer.exceptions.ValidationException;

public final class DateUtil {
    public static final DateTimeFormatter FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ROOT);

    private static final List<DateTimeFormatter> ACCEPTED = List.of(
        FORMAT,
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ROOT),
        DateTimeFormatter.ISO_LOCAL_DATE_TIME.withLocale(Locale.ROOT)
    );

    private DateUtil() {}

    public static String format(LocalDateTime dt) {
        return dt == null ? "-" : dt.format(FORMAT);
    }

    public static LocalDateTime parse(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new ValidationException(
                "Date is required (e.g. 2026-12-31 18:30)", ErrorCode.ERR_VALIDATION);
        }
        String input = s.trim();
        for (DateTimeFormatter f : ACCEPTED) {
            try {
                return LocalDateTime.parse(input, f);
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        throw new ValidationException(
            "Invalid date format. Use yyyy-MM-dd HH:mm, dd/MM/yyyy HH:mm, or ISO (yyyy-MM-ddTHH:mm).",
            ErrorCode.ERR_VALIDATION);
    }
}
