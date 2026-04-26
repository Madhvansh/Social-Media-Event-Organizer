package com.eventorganizer.ui.controllers;

import com.eventorganizer.models.Event;
import com.eventorganizer.models.FriendRequest;
import com.eventorganizer.models.Invitation;
import com.eventorganizer.models.Notification;
import com.eventorganizer.models.User;
import com.eventorganizer.models.dto.BatchInviteResult;
import com.eventorganizer.models.dto.UserActivityReport;
import com.eventorganizer.models.dto.UserProfileDTO;
import com.eventorganizer.models.enums.EventType;
import com.eventorganizer.models.enums.RSVPStatus;
import com.eventorganizer.services.EventService;
import com.eventorganizer.services.EventService.CreateEventResult;
import com.eventorganizer.services.FriendService;
import com.eventorganizer.services.InvitationService;
import com.eventorganizer.services.NotificationService;
import com.eventorganizer.services.RSVPService;
import com.eventorganizer.services.ReportService;
import com.eventorganizer.services.UserService;
import com.eventorganizer.store.DataStore;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Connects the UI to the service layer. All screen actions go through here. */
public class UIController {

    private final UserService userSvc = new UserService();
    private final EventService eventSvc = new EventService();
    private final FriendService friendSvc = new FriendService();
    private final InvitationService inviteSvc = new InvitationService();
    private final RSVPService rsvpSvc = new RSVPService();
    private final NotificationService notifSvc = new NotificationService();
    private final ReportService reportSvc = new ReportService();

    /* ---------- auth ---------- */

    public User register(String username, String email, char[] password) {
        return userSvc.register(username, email, password);
    }

    public User login(String username, char[] password) {
        return userSvc.login(username, password);
    }

    public void logout() { userSvc.logout(); }

    public User currentUser() { return DataStore.INSTANCE.getCurrentUser(); }

    /* ---------- profile ---------- */

    public UserProfileDTO getProfile() {
        return userSvc.getProfile(userSvc.requireCurrentUser());
    }

    public void updateProfile(String email, String bio) {
        userSvc.updateProfile(email, bio);
    }

    public void changePassword(char[] oldPw, char[] newPw) {
        userSvc.changePassword(oldPw, newPw);
    }

    /* ---------- events ---------- */

    public CreateEventResult createEvent(String name, String desc, LocalDateTime when,
                                         String location, EventType type) {
        return eventSvc.createEventWithWarnings(name, desc, when, location, type);
    }

    public Event editEvent(String eventId, String name, String desc, LocalDateTime when, String loc) {
        return eventSvc.editEvent(eventId, name, desc, when, loc);
    }

    public void cancelEvent(String eventId, String reason) {
        eventSvc.cancelEvent(eventId, reason);
    }

    public List<Event> myEvents()     { return eventSvc.viewMyEvents(); }
    public List<Event> myUpcoming()   { return eventSvc.viewUpcoming(); }
    public List<Event> myPast()       { return eventSvc.viewPast(); }

    /** All upcoming public events the current user did not create. */
    public List<Event> discoverPublicEvents() { return eventSvc.discoverPublicEvents(); }

    public Event viewEvent(String id) { return eventSvc.viewEventDetails(id); }

    /* ---------- invitations ---------- */

    public Invitation inviteFriend(String eventId, String username) {
        return inviteSvc.inviteFriend(eventId, username);
    }

    public BatchInviteResult inviteMany(String eventId, List<String> usernames) {
        return inviteSvc.inviteMany(eventId, usernames);
    }

    public void revokeInvitation(String eventId, String inviteeId) {
        inviteSvc.revoke(eventId, inviteeId);
    }

    public List<Invitation> viewInvitees(String eventId) {
        return inviteSvc.viewInvitees(eventId);
    }

    public List<Event> eventsInvitedTo() {
        return inviteSvc.eventsWithPendingInvitationsFor(userSvc.requireCurrentUser());
    }

    public Invitation joinPublicEvent(String eventId) {
        return inviteSvc.joinPublicEvent(eventId);
    }

    /* ---------- rsvp ---------- */

    public void respondRSVP(String eventId, RSVPStatus status) {
        rsvpSvc.respond(eventId, status);
    }

    public Map<RSVPStatus, Long> rsvpSummary(String eventId) {
        return rsvpSvc.viewRSVPSummary(eventId);
    }

    /* ---------- friends ---------- */

    public FriendRequest sendFriendRequest(String username) {
        return friendSvc.sendFriendRequest(username);
    }

    public void acceptFriendRequest(String requestId) { friendSvc.acceptFriendRequest(requestId); }
    public void rejectFriendRequest(String requestId) { friendSvc.rejectFriendRequest(requestId); }
    public void cancelSentRequest(String requestId)   { friendSvc.cancelSentRequest(requestId); }
    public void removeFriend(String username)         { friendSvc.removeFriend(username); }

    public List<User> friends()                { return friendSvc.listFriends(); }
    public List<FriendRequest> incomingReqs()  { return friendSvc.listIncomingRequests(); }

    /* ---------- notifications ---------- */

    public List<Notification> notifications()       { return notifSvc.getAllForCurrentUser(); }
    public List<Notification> unreadNotifications() { return notifSvc.getUnreadForCurrentUser(); }
    public int unreadCount()                        { return notifSvc.countUnread(); }
    public void markAllRead()                       { notifSvc.markAllRead(); }

    /* ---------- reports ---------- */

    public UserActivityReport activity() {
        return reportSvc.buildUserActivity(userSvc.requireCurrentUser());
    }

    /* ---------- helper ---------- */

    public User lookupUser(String userId) {
        return DataStore.INSTANCE.findUserById(userId).orElse(null);
    }
}
