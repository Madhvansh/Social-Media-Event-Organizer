package com.eventorganizer.services;

import com.eventorganizer.interfaces.Reportable;
import com.eventorganizer.models.Event;
import com.eventorganizer.models.Invitation;
import com.eventorganizer.models.User;
import com.eventorganizer.models.dto.EventSummaryReport;
import com.eventorganizer.models.dto.UserActivityReport;
import com.eventorganizer.models.enums.EventStatus;
import com.eventorganizer.models.enums.RSVPStatus;
import com.eventorganizer.store.DataStore;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ReportService {

    public int totalEventsCreatedBy(User user) {
        int count = 0;
        for (Event e : DataStore.INSTANCE.getAllEvents()) {
            if (e.getCreatorId().equals(user.getUserId())) count++;
        }
        return count;
    }

    public long totalAttendees(Event event) {
        return event.countByStatus(RSVPStatus.ACCEPTED);
    }

    public int upcomingEventsCount(User user) {
        int count = 0;
        for (Event e : DataStore.INSTANCE.getAllEvents()) {
            if (e.getCreatorId().equals(user.getUserId()) && e.isUpcoming()) count++;
        }
        return count;
    }

    public EventSummaryReport buildEventSummary(Event e) {
        Map<RSVPStatus, Long> counts = new EnumMap<>(RSVPStatus.class);
        for (RSVPStatus s : RSVPStatus.values()) counts.put(s, 0L);
        for (Invitation inv : e.getInvitations()) {
            counts.merge(inv.getStatus(), 1L, Long::sum);
        }
        return new EventSummaryReport(
            e.getEventId(), e.getName(), e.getType(), e.getStatus(),
            e.getDateTime(), e.getLocation(),
            e.getInvitations().size(), counts);
    }

    public UserActivityReport buildUserActivity(User user) {
        int total = 0, upcoming = 0, past = 0, cancelled = 0;
        long attendees = 0;
        List<EventSummaryReport> perEvent = new ArrayList<>();
        for (Event e : DataStore.INSTANCE.getAllEvents()) {
            if (!e.getCreatorId().equals(user.getUserId())) continue;
            total++;
            if (e.getStatus() == EventStatus.CANCELLED) cancelled++;
            else if (e.isUpcoming()) upcoming++;
            else if (e.isPast()) past++;
            attendees += totalAttendees(e);
            perEvent.add(buildEventSummary(e));
        }
        return new UserActivityReport(
            user.getUserId(), user.getUsername(),
            total, upcoming, past, cancelled, attendees, perEvent);
    }

    public String generateUserReport(User user) {
        int total    = totalEventsCreatedBy(user);
        int upcoming = upcomingEventsCount(user);
        long confirmedAttendees = 0;
        for (Event e : DataStore.INSTANCE.getAllEvents()) {
            if (e.getCreatorId().equals(user.getUserId())) {
                confirmedAttendees += totalAttendees(e);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("=== Report for ").append(user.getUsername()).append(" ===\n");
        sb.append("  Total events created:      ").append(total).append("\n");
        sb.append("  Upcoming events:           ").append(upcoming).append("\n");
        sb.append("  Past events:               ").append(total - upcoming).append("\n");
        sb.append("  Total confirmed attendees: ").append(confirmedAttendees).append("\n");
        return sb.toString();
    }

    public String generateEventSummary(Reportable reportable) {
        return reportable.generateSummary();
    }
}
