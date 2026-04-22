package com.eventorganizer.models.dto;

import java.util.Collections;
import java.util.List;

public final class UserActivityReport {
    private final String userId;
    private final String username;
    private final int totalEventsCreated;
    private final int upcomingEvents;
    private final int pastEvents;
    private final int cancelledEvents;
    private final long totalConfirmedAttendees;
    private final List<EventSummaryReport> perEvent;

    public UserActivityReport(String userId, String username,
                              int totalEventsCreated, int upcomingEvents, int pastEvents,
                              int cancelledEvents, long totalConfirmedAttendees,
                              List<EventSummaryReport> perEvent) {
        this.userId = userId;
        this.username = username;
        this.totalEventsCreated = totalEventsCreated;
        this.upcomingEvents = upcomingEvents;
        this.pastEvents = pastEvents;
        this.cancelledEvents = cancelledEvents;
        this.totalConfirmedAttendees = totalConfirmedAttendees;
        this.perEvent = perEvent == null ? Collections.emptyList() : List.copyOf(perEvent);
    }

    public String getUserId()                        { return userId; }
    public String getUsername()                      { return username; }
    public int getTotalEventsCreated()               { return totalEventsCreated; }
    public int getUpcomingEvents()                   { return upcomingEvents; }
    public int getPastEvents()                       { return pastEvents; }
    public int getCancelledEvents()                  { return cancelledEvents; }
    public long getTotalConfirmedAttendees()         { return totalConfirmedAttendees; }
    public List<EventSummaryReport> getPerEvent()    { return perEvent; }
}
