[README](README.md) • [UML Diagrams](UML.md)
  
# Event Organizer (Q7 — Social Media Event Organizer)

A Java desktop app that lets users register, manage their friends, create
public/private events, invite friends, RSVP, and read reports and notifications.
Built to showcase object-oriented design: every spec feature maps onto a
clear encapsulated model, abstract inheritance hierarchy, and polymorphic
dispatch. The UI is Swing + FlatLaf (dark theme); data lives in memory.

## Architecture

# Event Organizer — UML Class Diagrams

This document captures the full class structure of the Event Organizer
project as a set of focused UML class diagrams. Each diagram is rendered in
[Mermaid](https://mermaid.js.org/) syntax — GitHub, VS Code's Markdown
preview, and `mmdc` all render Mermaid natively.

The diagrams are split by concern so each one stays readable; together they
cover every class and relationship in the codebase.

| # | Diagram | Concern |
|---|---|---|
| 1 | [Architectural overview](#1-architectural-overview) | Package layout + dependency arrows |
| 2 | [Domain model — entities](#2-domain-model--entities) | `User`, `Event`, `Invitation`, `FriendRequest` and OOP relationships |
| 3 | [Polymorphic notifications](#3-polymorphic-notifications) | `Notification` abstract + 4 subclasses |
| 4 | [Enumerations](#4-enumerations) | The four enum types |
| 5 | [DTOs (report carriers)](#5-dtos-report-carriers) | Read-only reporting types |
| 6 | [Service layer](#6-service-layer) | Business-logic facades + their dependencies |
| 7 | [Persistence + DataStore](#7-persistence--datastore) | Singleton store, snapshot, ID generator |
| 8 | [Exception hierarchy](#8-exception-hierarchy) | `AppException` family used across layers |
| 9 | [UI controller seam](#9-ui-controller-seam) | How the Swing UI talks to services |

UML notation key (Mermaid `classDiagram`):

| Symbol | Meaning |
|---|---|
| `<|--` | inheritance (subclass → superclass) |
| `<|..` | interface implementation |
| `*--` | composition (lifetime owned) |
| `o--` | aggregation (lifetime independent) |
| `-->` | directional association / dependency |
| `..>` | dependency (uses but does not own) |
| `+` / `-` / `#` | public / private / protected member |
| `<<abstract>>` / `<<enum>>` / `<<interface>>` / `<<singleton>>` | stereotypes |

---

## 1. Architectural overview

A layered design with strict downward dependencies — the UI talks to a
controller, the controller talks to services, services talk to the
DataStore, and the DataStore talks to the model classes. Persistence + the
shutdown hook bracket the application lifecycle.

```mermaid
classDiagram
    direction TB

    class Main {
        <<entry>>
        +main(args)
    }

    class App {
        <<bootstrap>>
        +start()
    }

    class UIController {
        <<facade>>
    }

    class ServiceLayer {
        <<package>>
        UserService
        EventService
        InvitationService
        FriendService
        RSVPService
        NotificationService
        ReportService
    }

    class DataStore {
        <<singleton enum>>
    }

    class ModelLayer {
        <<package>>
        User · Event · Invitation
        FriendRequest · Notification
    }

    class Persistence {
        +load()
        +save()
    }

    class ShutdownHook
    class Preferences
    class FontLoader

    Main --> ShutdownHook : install
    Main --> Preferences  : load
    Main --> FontLoader   : register fonts
    Main --> Persistence  : load snapshot
    Main --> DataStore    : seed if empty
    Main --> App          : start

    App --> UIController     : owns
    UIController --> ServiceLayer : delegates
    ServiceLayer --> DataStore    : reads/writes
    DataStore    --> ModelLayer   : owns instances
    ServiceLayer --> ModelLayer   : creates
    ShutdownHook --> Persistence  : save on exit
    Persistence --> DataStore     : snapshot
```

---

## 2. Domain model — entities

Core OOP relationships. `Event` is abstract with two concrete subclasses;
`PrivateEvent.canInvite` overrides the friends-only rule. `User` and `Event`
both implement the `Reportable` interface so the `ReportService` can treat
them uniformly.

```mermaid
classDiagram
    direction LR

    class Reportable {
        <<interface>>
        +generateSummary() String
    }

    class User {
        -String userId
        -String username
        -String email
        -String passwordHash
        -byte[] salt
        -String bio
        -LocalDateTime createdAt
        -Set~String~ friendIds
        -List~String~ incomingFriendRequestIds
        -List~String~ outgoingFriendRequestIds
        -List~Notification~ notifications
        +verifyPassword(char[]) boolean
        +addFriend(User)
        +removeFriend(String)
        +addNotification(Notification)
        +markAllNotificationsRead()
        +generateSummary() String
    }

    class Event {
        <<abstract>>
        -String eventId
        -String name
        -String description
        -String location
        -LocalDateTime dateTime
        -String creatorId
        -EventStatus status
        -List~Invitation~ invitations
        +getType()* EventType
        +canInvite(User, User)* boolean
        +addInvitation(Invitation)
        +removeInvitation(Invitation) boolean
        +getInvitationForUser(String) Invitation
        +countByStatus(RSVPStatus) long
        +cancel()
        +isPast() boolean
        +isUpcoming() boolean
        +generateSummary() String
    }

    class PublicEvent {
        +getType() EventType
        +canInvite(User, User) boolean
    }

    class PrivateEvent {
        +getType() EventType
        +canInvite(User, User) boolean
    }

    class Invitation {
        -String invitationId
        -String eventId
        -String inviteeId
        -RSVPStatus status
        -LocalDateTime sentAt
        -LocalDateTime respondedAt
        -LocalDateTime updatedAt
        +respond(RSVPStatus)
        +equals(Object) boolean
        +hashCode() int
    }

    class FriendRequest {
        -String requestId
        -String senderId
        -String receiverId
        -FriendRequestStatus status
        -LocalDateTime sentAt
        -LocalDateTime resolvedAt
        +accept()
        +reject()
        +withdraw()
    }

    %% Inheritance
    Event <|-- PublicEvent
    Event <|-- PrivateEvent

    %% Interface implementation
    Reportable <|.. User
    Reportable <|.. Event

    %% Composition / aggregation
    Event "1" *-- "*" Invitation : owns
    User  "1" o-- "*" FriendRequest : participates in
    User  "*" -- "*" User : friends (by id)
```

Key OOP patterns visible here:

- **Inheritance + polymorphism:** `PublicEvent` / `PrivateEvent` override the
  abstract `getType()` and `canInvite()`. `EventService` works against the
  `Event` supertype; private-event invitation rules are dispatched at runtime.
- **Interface segregation:** `Reportable` lets `ReportService` treat heterogeneous
  classes (`User`, `Event`) uniformly without leaking persistence concerns.
- **Composite equality on `Invitation`:** `equals/hashCode` on `(eventId, inviteeId)` —
  enforced by `ModelInvariantsTest`.
- **Identity equality on `Event`, `User`, `FriendRequest`:** by primary id only.

---

## 3. Polymorphic notifications

The notification system is a textbook polymorphism example: one abstract
parent, four concrete subclasses, each with its own category label and
optional reference id (`eventId` or `requestId`). The UI's
`NotificationRow` renderer uses `getCategory()` to pick an icon + colour
without ever instanceof-checking.

```mermaid
classDiagram
    direction TB

    class Notification {
        <<abstract>>
        -String notificationId
        -String recipientId
        -String message
        -LocalDateTime timestamp
        -boolean read
        +getCategory()* String
        +markAsRead()
        +display() String
    }

    class InvitationNotification {
        -String eventId
        +getCategory() String
    }

    class EventUpdateNotification {
        -String eventId
        +getCategory() String
    }

    class RSVPNotification {
        -String eventId
        +getCategory() String
    }

    class FriendRequestNotification {
        -String requestId
        +getCategory() String
    }

    Notification <|-- InvitationNotification
    Notification <|-- EventUpdateNotification
    Notification <|-- RSVPNotification
    Notification <|-- FriendRequestNotification
```

---

## 4. Enumerations

Four enum types pin down the system's discrete states. They are
serializable and immutable; everything else in the model gates state
transitions through services.

```mermaid
classDiagram
    direction LR

    class EventType {
        <<enum>>
        PUBLIC
        PRIVATE
    }

    class EventStatus {
        <<enum>>
        ACTIVE
        CANCELLED
    }

    class RSVPStatus {
        <<enum>>
        PENDING
        ACCEPTED
        DECLINED
        MAYBE
    }

    class FriendRequestStatus {
        <<enum>>
        PENDING
        ACCEPTED
        REJECTED
        WITHDRAWN
    }
```

---

## 5. DTOs (report carriers)

Pure data objects produced by `ReportService` and `InvitationService`.
They cross the controller seam by value — no domain mutation possible
through them.

```mermaid
classDiagram
    direction LR

    class UserActivityReport {
        -long totalEventsCreated
        -long upcomingEvents
        -long pastEvents
        -long totalConfirmedAttendees
        -List~EventSummaryReport~ perEvent
    }

    class EventSummaryReport {
        -String eventId
        -String name
        -LocalDateTime dateTime
        -EventStatus status
        -EventType type
        -long totalInvited
        -Map~RSVPStatus,Long~ rsvpCounts
    }

    class UserProfileDTO {
        -String userId
        -String username
        -String email
        -String bio
        -LocalDateTime memberSince
        -int friendCount
        -int eventsCreated
    }

    class BatchInviteResult {
        -List~String~ invited
        -Map~String,String~ failures
        +addInvited(String)
        +addFailure(String, String)
    }

    UserActivityReport "1" *-- "*" EventSummaryReport : contains
```

---

## 6. Service layer

Seven services, each owning a single concern. They are stateless façades —
all state lives in `DataStore` — and they cross-call only through
`NotificationService` (which everyone uses to emit notifications).
`UIController` is the *only* outside caller; tests reach the services
directly.

```mermaid
classDiagram
    direction LR

    class UserService {
        +register(String, String, char[]) User
        +login(String, char[]) User
        +logout()
        +updateProfile(String, String)
        +changePassword(char[], char[])
        +requireCurrentUser() User
        +getProfile(User) UserProfileDTO
    }

    class EventService {
        +createEvent(...) Event
        +createEventWithWarnings(...) CreateEventResult
        +editEvent(...) Event
        +cancelEvent(String, String)
        +viewMyEvents() List~Event~
        +viewUpcoming() List~Event~
        +viewPast() List~Event~
        +discoverPublicEvents() List~Event~
        +viewEventDetails(String) Event
    }

    class FriendService {
        +sendFriendRequest(String) FriendRequest
        +acceptFriendRequest(String)
        +rejectFriendRequest(String)
        +cancelSentRequest(String)
        +removeFriend(String)
        +listFriends() List~User~
        +listIncomingRequests() List~FriendRequest~
    }

    class InvitationService {
        +inviteFriend(String, String) Invitation
        +inviteMany(String, List) BatchInviteResult
        +revoke(String, String)
        +viewInvitees(String) List~Invitation~
        +eventsWithPendingInvitationsFor(User) List~Event~
        +joinPublicEvent(String) Invitation
    }

    class RSVPService {
        +respond(String, RSVPStatus)
        +viewRSVPSummary(String) Map~RSVPStatus,Long~
    }

    class NotificationService {
        +push(User, Notification)
        +pushCoalesced(User, Notification)
        +getAllForCurrentUser() List~Notification~
        +getUnreadForCurrentUser() List~Notification~
        +countUnread() int
        +markAllRead()
    }

    class ReportService {
        +buildUserActivity(User) UserActivityReport
    }

    EventService        ..> NotificationService : notifies invitees
    InvitationService   ..> NotificationService : sends invite/revoke
    FriendService       ..> NotificationService : friend events
    RSVPService         ..> NotificationService : creator updates
    UserService         ..> NotificationService : welcome events
    ReportService       ..> EventService        : reads events

    UserService         ..> DataStore
    EventService        ..> DataStore
    FriendService       ..> DataStore
    InvitationService   ..> DataStore
    RSVPService         ..> DataStore
    NotificationService ..> DataStore
    ReportService       ..> DataStore

    class DataStore {
        <<singleton enum>>
    }
```

---

## 7. Persistence + DataStore

`DataStore` is an enum singleton — guaranteed thread-safe init by the JVM
and trivial to mock via `TestHooks`. `Persistence` snapshots it via Java
serialization to a single file (`eventorganizer.data`) at JVM exit, then
loads it on next launch so accounts, events, invitations, and friendships
all survive between runs.

```mermaid
classDiagram
    direction TB

    class DataStore {
        <<singleton enum>>
        -ConcurrentHashMap~String,User~ usersById
        -ConcurrentHashMap~String,Event~ eventsById
        -ConcurrentHashMap~String,FriendRequest~ friendRequestsById
        -ConcurrentHashMap~String,Map~ invitationIndex
        -volatile User currentUser
        -volatile boolean seeded
        +saveUser(User)
        +saveEvent(Event)
        +saveFriendRequest(FriendRequest)
        +findUserById(String) Optional~User~
        +findUserByUsername(String) Optional~User~
        +findEventById(String) Optional~Event~
        +indexInvitation(Invitation)
        +unindexInvitation(String, String)
        +eventIdsForCreator(String) Set~String~
        +setCurrentUser(User)
        +seed()
        +markSeeded()
    }

    class Persistence {
        <<utility>>
        +save() void
        +load() boolean
        +snapshotPath() Path
    }

    class Snapshot {
        <<inner-Serializable>>
        byte version
        List~User~ users
        List~Event~ events
        List~FriendRequest~ friendRequests
        long[] nextIds
    }

    class IdGenerator {
        <<utility>>
        +nextUserId() String
        +nextEventId() String
        +nextNotificationId() String
        +nextInvitationId() String
        +nextFriendRequestId() String
        +snapshot() long[]
        +restore(long[])
    }

    class TestHooks {
        <<test>>
        +reset()
        +installFixedClock(Clock)
    }

    class ShutdownHook {
        +install()
    }

    Persistence ..> DataStore   : reads/writes
    Persistence *-- Snapshot    : serializes
    Persistence ..> IdGenerator : counter snapshot
    ShutdownHook ..> Persistence : save on exit
    TestHooks    ..> DataStore  : reset state
```

---

## 8. Exception hierarchy

A 3-level tree under `AppException`. Each leaf carries an `ErrorCode` so
the UI's single try/catch seam can map by code without string matching.
`UnauthorizedException` is the only 4-deep node — it specialises
`AuthorizationException` for "no current session" specifically.

```mermaid
classDiagram
    direction TB

    class RuntimeException

    class AppException {
        -ErrorCode errorCode
        +getErrorCode() ErrorCode
    }

    class ValidationException
    class AuthenticationException
    class AuthorizationException
    class InvalidOperationException

    class NotFoundException
    class UserNotFoundException
    class EventNotFoundException
    class InvitationNotFoundException

    class ConflictException
    class DuplicateUsernameException
    class DuplicateEmailException
    class DuplicateInvitationException
    class DuplicateFriendRequestException

    class UnauthorizedException

    class ErrorCode {
        <<enum>>
        ERR_VALIDATION
        ERR_AUTH
        ERR_NOT_FOUND
        ERR_CONFLICT
        ERR_INVALID_OP
        ...
    }

    RuntimeException <|-- AppException
    AppException <|-- ValidationException
    AppException <|-- AuthenticationException
    AppException <|-- AuthorizationException
    AppException <|-- InvalidOperationException
    AppException <|-- NotFoundException
    AppException <|-- ConflictException

    NotFoundException <|-- UserNotFoundException
    NotFoundException <|-- EventNotFoundException
    NotFoundException <|-- InvitationNotFoundException

    ConflictException <|-- DuplicateUsernameException
    ConflictException <|-- DuplicateEmailException
    ConflictException <|-- DuplicateInvitationException
    ConflictException <|-- DuplicateFriendRequestException

    AuthorizationException <|-- UnauthorizedException

    AppException ..> ErrorCode : carries
```

---

## 9. UI controller seam

The Swing UI never touches `DataStore` or any service directly. Every UI
action funnels through `UIController`, which translates each call into a
service call. `Toast.error` at the call site catches every `AppException`
that bubbles up — that's the single error-handling seam for the entire
front-end.

```mermaid
classDiagram
    direction TB

    class App {
        <<bootstrap>>
        +start()
    }

    class CanvasPanel {
        <<aurora root>>
    }

    class AuthScreen
    class DashboardScreen
    class MyEventsPanel
    class DiscoverEventsPanel
    class FriendsPanel
    class NotificationsPanel
    class ReportsPanel
    class ProfilePanel

    class EventDetailsDialog
    class CreateEventDialog
    class EditEventDialog
    class InviteFriendsDialog
    class ConfirmDialog
    class ChangePasswordDialog

    class UIController {
        <<facade>>
        +login(String, char[])
        +register(String, String, char[])
        +createEvent(...)
        +editEvent(...)
        +cancelEvent(String, String)
        +inviteMany(String, List)
        +respondRSVP(String, RSVPStatus)
        +sendFriendRequest(String)
        +discoverPublicEvents() List~Event~
        +activity() UserActivityReport
        +notifications() List~Notification~
    }

    class ServiceLayer {
        <<package>>
    }

    App --> CanvasPanel : owns
    CanvasPanel --> AuthScreen : card
    CanvasPanel --> DashboardScreen : card

    DashboardScreen *-- MyEventsPanel
    DashboardScreen *-- DiscoverEventsPanel
    DashboardScreen *-- FriendsPanel
    DashboardScreen *-- NotificationsPanel
    DashboardScreen *-- ReportsPanel
    DashboardScreen *-- ProfilePanel

    AuthScreen           ..> UIController
    MyEventsPanel        ..> UIController
    DiscoverEventsPanel  ..> UIController
    FriendsPanel         ..> UIController
    NotificationsPanel   ..> UIController
    ReportsPanel         ..> UIController
    ProfilePanel         ..> UIController

    EventDetailsDialog   ..> UIController
    CreateEventDialog    ..> UIController
    EditEventDialog      ..> UIController
    InviteFriendsDialog  ..> UIController
    ConfirmDialog        ..> UIController
    ChangePasswordDialog ..> UIController

    UIController --> ServiceLayer : delegates
```

---

## OOP design principles map

| Principle | Where it lives in the code |
|---|---|
| **Encapsulation** | All model fields are `private`; mutation only via setters that delegate to `Validator` (e.g. `Event.setLocation`, `User.updateProfile`). `passwordHash` and `salt` are read-only after construction except via `setPasswordHash`. |
| **Inheritance** | `Event ← PublicEvent / PrivateEvent`, `Notification ← {Invitation, EventUpdate, RSVP, FriendRequest}Notification`, exception 3-level tree. |
| **Polymorphism** | `Event.getType()` / `canInvite()` dispatched at runtime; `ReportService` treats `Reportable` uniformly across `User` and `Event`. `NotificationService` pushes any `Notification` subtype. |
| **Abstraction** | `Event`, `Notification` are abstract; `Reportable` is the lone interface. The UI talks only to `UIController` (façade), never to services or the store. |
| **Composition** | `Event` *owns* its `Invitation` list (lifetime-bound); `User` *owns* its `Notification` list. |
| **Aggregation** | `User` *references* friends by `userId` (lifetime-independent). `DataStore` aggregates all entities by id. |
| **Singleton** | `DataStore` is an `enum INSTANCE` — JVM-guaranteed thread-safe init. |
| **Façade** | `UIController` is the single entry point from the Swing layer to the seven services. |
| **Strategy** | `Easing` curves and `AuroraButton.Variant` (`DEFAULT`/`OUTLINE`/`GHOST`/`DANGER`) pick the rendering strategy per instance. |
| **Observer** | `Motion.addListener` lets `CanvasPanel` and `Constellation` pause their ambient timers the instant the user toggles reduced motion. |

---

## Rendering instructions

To regenerate these as PNG/SVG (e.g. for a written report):

```bash
# install once
npm install -g @mermaid-js/mermaid-cli

# render every code block in this file
mmdc -i docs/UML.md -o docs/UML.png
```


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
