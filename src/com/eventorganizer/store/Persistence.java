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


public final class Persistence {
    private Persistence() {}

    public static final String FILE_NAME = "eventorganizer.data";
    private static final byte CURRENT_VERSION = 1;
    private static final Logger LOG = Logger.getLogger(Persistence.class.getName());

   
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
            
            try {
                Files.move(tmp.toPath(), target.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex2) {
                LOG.log(Level.WARNING, "Persistence.save: failed to rename tmp file", ex2);
            }
        }
    }

    
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
        if (snap.users != null) {
            for (User u : snap.users) {
                ds.saveUser(u);
            }
        }
        if (snap.events != null) {
            for (Event e : snap.events) {
                ds.saveEvent(e);
                for (Invitation inv : e.getInvitations()) {
                    ds.indexInvitation(inv);
                }
            }
        }
        if (snap.friendRequests != null) {
            for (FriendRequest r : snap.friendRequests) {
                ds.saveFriendRequest(r);
            }
        }
        if (snap.nextIds != null) {
            com.eventorganizer.utils.IdGenerator.restore(snap.nextIds);
        }
        ds.markSeeded();
        return true;
    }

    
    public static Path snapshotPath() {
        return new File(FILE_NAME).getAbsoluteFile().toPath();
    }

    
    private static final class Snapshot implements Serializable {
        private static final long serialVersionUID = 1L;
        byte version;
        List<User> users;
        List<Event> events;
        List<FriendRequest> friendRequests;
        long[] nextIds;
    }
}
