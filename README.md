[README](README.md) • [UML Diagrams](UML.md)
  
# Event Organizer (Q7 — Social Media Event Organizer)

A Java desktop app that lets users register, manage their friends, create
public/private events, invite friends, RSVP, and read reports and notifications.
Built to showcase object-oriented design: every spec feature maps onto a
clear encapsulated model, abstract inheritance hierarchy, and polymorphic
dispatch. The UI is Swing + FlatLaf (dark theme); data lives in memory.

## Architecture

```
+-------------------------------------------------------------+
|  UI  (Swing + FlatLaf)                                      |
|  ui/App, ui/screens/{Auth,Dashboard,panels/...}, dialogs/   |
|                           |                                 |
|                           v                                 |
|  UIController  (single facade, 1 try/catch seam)            |
|                           |                                 |
|                           v                                 |
|  Services: UserService, EventService, InvitationService,    |
|            FriendService, RSVPService, NotificationService, |
|            ReportService                                    |
|                           |                                 |
|                           v                                 |
|  Models:  User, Event(PublicEvent/PrivateEvent), Invitation,|
|           FriendRequest, Notification + 4 subclasses, DTOs  |
|                           |                                 |
|                           v                                 |
|  Store:   DataStore (enum singleton; usernameIndex,         |
|           emailIndex, invitationIndex, friendRequestIndex)  |
+-------------------------------------------------------------+
```

Exceptions are unchecked and rooted at `AppException`; every service throws a
typed subclass on failure and the UI catches once at the screen seam and
surfaces a `Toast`. Time reads flow through `DataStore.INSTANCE.getClock()`
so tests can inject a fixed clock.

## Build / run / test

```bash
# Compile (Unix)
./build.sh
# Compile (Windows)
build.bat

# Launch the app
./run.sh    # or run.bat

# Run the JUnit 5 suite (68 tests)
./test.sh   # or test.bat
```

Dependencies vendored under `lib/`:
- `flatlaf-3.4.jar` (dark theme)
- `junit-platform-console-standalone-1.10.2.jar` (test runner)

No Maven / Gradle required. JDK 11+ is sufficient.

Seeded accounts (ready on first launch):
- `alice / alice123`
- `bob / bob12345`
- `carol / carol123`

The seed also populates cross-user state (pending friend request,
invitations, RSVPs) so every panel shows content on alice's first login.

## Spec features (7) → entry points

| # | Feature | Entry point |
|---|---|---|
| 1 | User registration / login / profile | `services/UserService.java` |
| 2 | Create / edit / cancel events | `services/EventService.java` |
| 3 | Send / accept / reject / withdraw friend requests | `services/FriendService.java` |
| 4 | Invite friends (public + private rules) | `services/InvitationService.java` |
| 5 | RSVP (accept / decline / maybe) | `services/RSVPService.java` |
| 6 | Notifications (4 kinds, cap + coalesce) | `services/NotificationService.java` |
| 7 | Reports (per-user activity + per-event summary) | `services/ReportService.java` |

## OOP principles

See [docs/OOP_EVIDENCE.md](docs/OOP_EVIDENCE.md) for a complete file:line map.
Highlights:

- `abstract class Event` with `canInvite` overridden by `PublicEvent` /
  `PrivateEvent` — polymorphic dispatch at `InvitationService.java:51`.
- `abstract class Notification` with 4 concrete subclasses, each supplying a
  `getCategory()`.
- `interface Reportable` implemented by both `Event` and `User`.
- `enum DataStore { INSTANCE; }` — the canonical singleton.
- 68 JUnit 5 tests (`test/com/eventorganizer/`) — all passing.

## Smoke test

See [docs/SMOKE_TEST.md](docs/SMOKE_TEST.md) for a 10-step UI smoke test.
