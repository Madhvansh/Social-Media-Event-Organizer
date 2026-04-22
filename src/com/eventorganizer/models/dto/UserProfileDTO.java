package com.eventorganizer.models.dto;

import java.time.LocalDateTime;

public final class UserProfileDTO {
    private final String userId;
    private final String username;
    private final String email;
    private final String bio;
    private final int friendCount;
    private final int eventsCreated;
    private final LocalDateTime memberSince;

    public UserProfileDTO(String userId, String username, String email, String bio,
                          int friendCount, int eventsCreated, LocalDateTime memberSince) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.friendCount = friendCount;
        this.eventsCreated = eventsCreated;
        this.memberSince = memberSince;
    }

    public String getUserId()                  { return userId; }
    public String getUsername()                { return username; }
    public String getEmail()                   { return email; }
    public String getBio()                     { return bio; }
    public int getFriendCount()                { return friendCount; }
    public int getEventsCreated()              { return eventsCreated; }
    public LocalDateTime getMemberSince()      { return memberSince; }
}
