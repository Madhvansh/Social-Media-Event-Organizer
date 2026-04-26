[README](README.md) • [UML Diagrams](docs/UML.md) • [Presentation](OOPs%20Project%20Presentation.pdf)

# Event Organizer

A Java desktop application I built for managing social events, friends, and
invitations. Users can register, create public or private events, invite friends,
RSVP, and view notifications and activity reports. The UI is built with Swing and
FlatLaf (dark theme) and all data lives in memory.

# Architecture

## UML Class Diagrams

The diagrams are split by concern so each one stays readable.

| # | Diagram | Concern |
|---|---|---|
| 1 | [Architectural overview](#1-architectural-overview) | Package layout + dependency arrows |
| 2 | [Domain model — entities](#2-domain-model--entities) | `User`, `Event`, `Invitation`, `FriendRequest` |
| 3 | [Notifications](#3-notifications) | `Notification` abstract + 4 subclasses |
| 4 | [Enumerations](#4-enumerations) | The four enum types |
| 5 | [DTOs (report carriers)](#5-dtos-report-carriers) | Read-only reporting types |
| 6 | [Service layer](#6-service-layer) | Business-logic classes + their dependencies |
| 7 | [Persistence + DataStore](#7-persistence--datastore) | Data store, snapshot, ID generator |
| 8 | [Exception hierarchy](#8-exception-hierarchy) | `AppException` family |
| 9 | [UI controller seam](#9-ui-controller-seam) | How the Swing UI talks to services |

---

## 1. Architectural overview

Shows how the main parts of the app are connected.

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

The main data classes and how they relate to each other.

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

---

## 3. Notifications

The four types of notification, each with its own category.

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

The four enums used across the app.

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

Data objects used for reports.

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

The seven service classes and their dependencies.

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

DataStore holds all the data. Persistence saves and loads it.

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

Custom exception types used throughout the app.

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

How the Swing UI talks to the service layer.

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

## How to Run

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

## Seeded accounts

The app comes with a few sample accounts ready to use on first launch:
- `alice / alice123`
- `bob / bob12345`
- `carol / carol123`

These accounts already have some cross-user state set up (a pending friend request,
invitations, RSVPs) so every panel shows content straight away when you log in as alice.

## Where to find the logic

The core business logic lives in `src/com/eventorganizer/services/`. Each service
handles one area of the app:

- `UserService` — registration, login, profile, and password changes
- `EventService` — creating, editing, and cancelling events
- `FriendService` — sending, accepting, rejecting, and cancelling friend requests
- `InvitationService` — inviting friends, revoking invites, joining public events
- `RSVPService` — responding to invitations (accept / decline / maybe)
- `NotificationService` — pushing and reading notifications
- `ReportService` — building activity and event summary reports

The UI talks to all of these through `UIController`, so the screens never access
the data store directly.

## Implementation Highlights

- I used an abstract `Event` class with two concrete subclasses (`PublicEvent` and
  `PrivateEvent`). Each one overrides the `canInvite()` method to enforce different
  invitation rules — private events only allow the creator's friends to be invited.

- I modelled notifications the same way: an abstract `Notification` class with four
  concrete types (`InvitationNotification`, `EventUpdateNotification`,
  `RSVPNotification`, `FriendRequestNotification`). The UI reads `getCategory()` on
  each one to pick the right icon, without ever checking which subclass it is.

- Both `Event` and `User` implement the `Reportable` interface, which lets
  `ReportService` generate summaries for either type without caring about the
  specific class.

- I chose to use an enum for `DataStore` so there is always exactly one instance of
  the data store running in the app. It saves and loads to a file (`eventorganizer.data`)
  on startup and shutdown, so accounts and events survive between runs.

- I included 68 JUnit tests in `test/com/eventorganizer/` that cover the model
  behaviour and service logic.
