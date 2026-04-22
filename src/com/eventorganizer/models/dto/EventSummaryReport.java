package com.eventorganizer.models.dto;

import com.eventorganizer.models.enums.EventStatus;
import com.eventorganizer.models.enums.EventType;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

import com.eventorganizer.models.enums.RSVPStatus;

public final class EventSummaryReport {
    private final String eventId;
    private final String name;
    private final EventType type;
    private final EventStatus status;
    private final LocalDateTime dateTime;
    private final String location;
    private final int totalInvited;
    private final Map<RSVPStatus, Long> rsvpCounts;

    public EventSummaryReport(String eventId, String name, EventType type, EventStatus status,
                              LocalDateTime dateTime, String location, int totalInvited,
                              Map<RSVPStatus, Long> rsvpCounts) {
        this.eventId = eventId;
        this.name = name;
        this.type = type;
        this.status = status;
        this.dateTime = dateTime;
        this.location = location;
        this.totalInvited = totalInvited;
        this.rsvpCounts = new EnumMap<>(rsvpCounts);
    }

    public String getEventId()                    { return eventId; }
    public String getName()                       { return name; }
    public EventType getType()                    { return type; }
    public EventStatus getStatus()                { return status; }
    public LocalDateTime getDateTime()            { return dateTime; }
    public String getLocation()                   { return location; }
    public int getTotalInvited()                  { return totalInvited; }
    public Map<RSVPStatus, Long> getRsvpCounts()  { return new EnumMap<>(rsvpCounts); }

    public long count(RSVPStatus s) {
        return rsvpCounts.getOrDefault(s, 0L);
    }
}
