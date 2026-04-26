package com.eventorganizer.services;

import com.eventorganizer.exceptions.AuthenticationException;
import com.eventorganizer.exceptions.DuplicateEmailException;
import com.eventorganizer.exceptions.DuplicateUsernameException;
import com.eventorganizer.exceptions.ErrorCode;
import com.eventorganizer.exceptions.InvalidOperationException;
import com.eventorganizer.exceptions.UnauthorizedException;
import com.eventorganizer.exceptions.ValidationException;
import com.eventorganizer.models.User;
import com.eventorganizer.models.dto.UserProfileDTO;
import com.eventorganizer.store.DataStore;
import com.eventorganizer.utils.IdGenerator;
import com.eventorganizer.utils.Limits;
import com.eventorganizer.utils.Normalize;
import com.eventorganizer.utils.PasswordHasher;
import com.eventorganizer.utils.Validator;

public class UserService {

    public User register(String username, String email, char[] rawPassword) {
        Validator.requireNonBlank(username, "Username");
        Validator.requireLength(username, Limits.USERNAME_MAX, "Username");
        Validator.requireNonBlank(email, "Email");
        Validator.requireLength(email, Limits.EMAIL_MAX, "Email");
        if (Normalize.containsAmbiguousCharacters(username.trim())) {
            throw new ValidationException(
                "Username contains ambiguous characters. Use ASCII letters, digits, or underscores only.",
                ErrorCode.ERR_VALIDATION);
        }
        if (!Validator.isValidUsername(username.trim()))
            throw new ValidationException(
                "Username must contain only letters, digits, or underscores.", ErrorCode.ERR_VALIDATION);
        if (!Validator.isValidEmail(email))
            throw new ValidationException("Invalid email format.", ErrorCode.ERR_VALIDATION);
        if (!Validator.isPasswordStrong(rawPassword))
            throw new ValidationException(
                "Password must be 6-128 characters with at least 1 letter and 1 digit.",
                ErrorCode.ERR_VALIDATION);

        DataStore ds = DataStore.INSTANCE;
        String trimmedUsername = username.trim();
        String trimmedEmail = email.trim();
        if (ds.usernameExists(trimmedUsername)) {
            throw new DuplicateUsernameException(
                "Username '" + trimmedUsername + "' is already taken.");
        }
        if (ds.emailExists(trimmedEmail)) {
            throw new DuplicateEmailException(
                "Email '" + trimmedEmail + "' is already registered.");
        }

        byte[] salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash(rawPassword, salt);
        User u = new User(IdGenerator.nextUserId(), trimmedUsername, trimmedEmail, hash, salt);
        ds.saveUser(u);
        return u;
    }

    /** String overload — converts to char[] and calls the main register method. */
    public User register(String username, String email, String rawPassword) {
        return register(username, email, rawPassword == null ? new char[0] : rawPassword.toCharArray());
    }

    public User login(String username, char[] rawPassword) {
        DataStore ds = DataStore.INSTANCE;
        User u = ds.findUserByUsername(username).orElse(null);
        if (u == null) {
            // run the same hash check even if the username doesn't exist, so timing is consistent
            PasswordHasher.burnDummyVerify(rawPassword);
            throw new AuthenticationException("Invalid credentials",
                ErrorCode.ERR_AUTH_INVALID_CREDENTIALS);
        }
        if (!u.verifyPassword(rawPassword)) {
            throw new AuthenticationException("Invalid credentials",
                ErrorCode.ERR_AUTH_INVALID_CREDENTIALS);
        }
        // if the stored hash is outdated, update it quietly on login
        if (PasswordHasher.needsRehash(u.getPasswordHash())) {
            u.setPasswordHash(PasswordHasher.hash(rawPassword, u.getSalt()));
        }
        ds.setCurrentUser(u);
        return u;
    }

    public User login(String username, String rawPassword) {
        return login(username, rawPassword == null ? new char[0] : rawPassword.toCharArray());
    }

    public void logout() {
        DataStore.INSTANCE.clearSession();
    }

    public User requireCurrentUser() {
        User u = DataStore.INSTANCE.getCurrentUser();
        if (u == null) throw new UnauthorizedException("You must be logged in to perform this action.");
        return u;
    }

    public void changePassword(char[] oldRaw, char[] newRaw) {
        User u = requireCurrentUser();
        if (!u.verifyPassword(oldRaw)) {
            throw new AuthenticationException(
                "Current password is incorrect.", ErrorCode.ERR_AUTH_INVALID_CREDENTIALS);
        }
        if (!Validator.isPasswordStrong(newRaw)) {
            throw new ValidationException(
                "Password must be 6-128 characters with at least 1 letter and 1 digit.",
                ErrorCode.ERR_VALIDATION);
        }
        String newHash = PasswordHasher.hash(newRaw, u.getSalt());
        u.setPasswordHash(newHash);
    }

    public void updateProfile(String email, String bio) {
        User u = requireCurrentUser();
        if (email != null && !email.trim().isEmpty()) {
            Validator.requireLength(email, Limits.EMAIL_MAX, "Email");
            if (!Validator.isValidEmail(email)) {
                throw new ValidationException("Invalid email format.", ErrorCode.ERR_VALIDATION);
            }
            String trimmed = email.trim();
            DataStore ds = DataStore.INSTANCE;
            // If the new email is already taken by another user, reject.
            ds.findUserByEmail(trimmed).ifPresent(other -> {
                if (!other.getUserId().equals(u.getUserId())) {
                    throw new DuplicateEmailException(
                        "Email '" + trimmed + "' is already registered.");
                }
            });
            u.setEmail(trimmed);
        }
        if (bio != null) {
            Validator.requireLength(bio, Limits.BIO_MAX, "Bio");
            u.setBio(bio);
        }
    }

    public UserProfileDTO getProfile(User user) {
        if (user == null) throw new InvalidOperationException("user is required");
        int eventsCreated = DataStore.INSTANCE.eventIdsForCreator(user.getUserId()).size();
        return new UserProfileDTO(
            user.getUserId(),
            user.getUsername(),
            user.getEmail(),
            user.getBio(),
            user.getFriendCount(),
            eventsCreated,
            user.getCreatedAt());
    }

    public String viewProfile() {
        User u = requireCurrentUser();
        StringBuilder sb = new StringBuilder();
        sb.append("=== Profile ===\n");
        sb.append("  ID:       ").append(u.getUserId()).append("\n");
        sb.append("  Username: ").append(u.getUsername()).append("\n");
        sb.append("  Email:    ").append(u.getEmail()).append("\n");
        sb.append("  Bio:      ").append(u.getBio().isEmpty() ? "(none)" : u.getBio()).append("\n");
        sb.append("  Friends:  ").append(u.getFriendCount()).append("\n");
        return sb.toString();
    }
}
