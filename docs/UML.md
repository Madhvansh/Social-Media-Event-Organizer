# Event Organizer — UML Class Diagrams

Class diagrams for the main components.

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
