package com.eventorganizer.models.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class BatchInviteResult {
    private final List<String> invited;
    private final List<Failure> failures;

    public BatchInviteResult() {
        this.invited = new ArrayList<>();
        this.failures = new ArrayList<>();
    }

    public void addInvited(String username) {
        invited.add(username);
    }

    public void addFailure(String username, String reason) {
        failures.add(new Failure(username, reason));
    }

    public List<String> getInvited()         { return Collections.unmodifiableList(invited); }
    public List<Failure> getFailures()       { return Collections.unmodifiableList(failures); }
    public int getInvitedCount()             { return invited.size(); }
    public int getFailureCount()             { return failures.size(); }

    public static final class Failure {
        private final String username;
        private final String reason;
        public Failure(String username, String reason) {
            this.username = username;
            this.reason = reason;
        }
        public String getUsername() { return username; }
        public String getReason()   { return reason; }
    }
}
