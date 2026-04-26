package com.eventorganizer.store;

import com.eventorganizer.models.Event;
import com.eventorganizer.models.FriendRequest;
import com.eventorganizer.models.Invitation;
import com.eventorganizer.models.User;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Saves and restores the {@link DataStore} between runs. Uses Java
 * serialization on a wrapper {@link Snapshot} so all model classes
 * (User / Event / Invitation / FriendRequest / Notification) are written
 * out together.
 *
 * <p>The on-disk file is {@value #FILE_NAME} in the working directory. Writes
 * are atomic (write to {@code .tmp} then rename) so a crash mid-save can't
 * leave a half-written file.
 *
 * <p>Versioning: snapshots embed a {@link Snapshot#version} byte. If the
 * version on disk is newer than what this build understands, load is skipped
 * with a warning instead of throwing.
 */
public final class Persistence {
    private Persistence() {}

    public static final String FILE_NAME = "eventorganizer.data";
    private static final byte CURRENT_VERSION = 1;
    private static final Logger LOG = Logger.getLogger(Persistence.class.getName());

    /** Saves the entire DataStore to {@link #FILE_NAME}. Atomic via tmp+rename. */
    public static synchronized void save() {
        save(new File(FILE_NAME));
    }

    public static synchronized void save(File target) {
        Snapshot snap = new Snapshot();
        snap.version = CURRENT_VERSION;
        snap.users = new ArrayList<>(DataStore.INSTANCE.getAllUsers());
        snap.events = new ArrayList<>(DataStore.INSTANCE.getAllEvents());
        snap.friendRequests = new ArrayList<>(DataStore.INSTANCE.getAllFriendRequests());
        snap.nextIds = com.eventorganizer.utils.IdGenerator.snapshot();

        File parent = target.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();

        File tmp = new File(target.getParentFile(), target.getName() + ".tmp");
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(tmp)))) {
            oos.writeObject(snap);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Persistence.save: failed to write " + target, ex);
            return;
        }
        try {
            Files.move(tmp.toPath(), target.toPath(),
                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ex) {
            // ATOMIC_MOVE may fail across filesystems; fall back to non-atomic.
            try {
                Files.move(tmp.toPath(), target.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex2) {
                LOG.log(Level.WARNING, "Persistence.save: failed to rename tmp file", ex2);
            }
        }
    }

    /**
     * Restores DataStore from {@link #FILE_NAME} if it exists. Returns true
     * if a snapshot was loaded, false if no file present (caller can then
     * fall back to {@link DataStore#seed()}).
     */
    public static synchronized boolean load() {
        return load(new File(FILE_NAME));
    }

    public static synchronized boolean load(File source) {
        if (!source.isFile()) return false;
        Snapshot snap;
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(source)))) {
            Object o = ois.readObject();
            if (!(o instanceof Snapshot)) {
                LOG.warning("Persistence.load: snapshot file is not a Snapshot; ignoring.");
                return false;
            }
            snap = (Snapshot) o;
        } catch (IOException | ClassNotFoundException ex) {
            LOG.log(Level.WARNING, "Persistence.load: failed to read " + source
                + " — starting from seed.", ex);
            return false;
        }

        if (snap.version > CURRENT_VERSION) {
            LOG.warning("Persistence.load: snapshot version " + snap.version
                + " is newer than supported (" + CURRENT_VERSION + "). Ignoring.");
            return false;
        }

        DataStore ds = DataStore.INSTANCE;
        // Restore users (rebuilds the username + email indices).
        if (snap.users != null) {
            for (User u : snap.users) {
                ds.saveUser(u);
            }
        }
        // Restore events and reattach invitation indices.
        if (snap.events != null) {
            for (Event e : snap.events) {
                ds.saveEvent(e);
                for (Invitation inv : e.getInvitations()) {
                    ds.indexInvitation(inv);
                }
            }
        }
        // Restore friend requests.
        if (snap.friendRequests != null) {
            for (FriendRequest r : snap.friendRequests) {
                ds.saveFriendRequest(r);
            }
        }
        // Restore the ID generator counters so new ids don't collide.
        if (snap.nextIds != null) {
            com.eventorganizer.utils.IdGenerator.restore(snap.nextIds);
        }
        // Mark as already-seeded so the demo seed doesn't run.
        ds.markSeeded();
        return true;
    }

    /** Resolves the snapshot path used by {@link #save()} / {@link #load()}. */
    public static Path snapshotPath() {
        return new File(FILE_NAME).getAbsoluteFile().toPath();
    }

    /** Snapshot wire format. Must remain backward-compatible across versions. */
    private static final class Snapshot implements Serializable {
        private static final long serialVersionUID = 1L;
        byte version;
        List<User> users;
        List<Event> events;
        List<FriendRequest> friendRequests;
        long[] nextIds;
    }
}
