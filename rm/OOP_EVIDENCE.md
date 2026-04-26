# OOP Evidence Map

Each row points to the file and line where the principle is most directly demonstrated.

| Principle | File:Line | Description |
|---|---|---|
| **Encapsulation** | `src/com/eventorganizer/models/User.java:26` | `friendIds` is `private final`, exposed only via `isFriendWith`, `getFriendIds` (unmodifiable view), `getFriendCount`. |
| **Encapsulation (ID immutability)** | `src/com/eventorganizer/models/Event.java:16` | `eventId` is `private final`; no setter. Same pattern on `User.userId`, `Invitation.invitationId`, `FriendRequest.requestId`, `Notification.notificationId`. Enforced by reflection in `ModelInvariantsTest.primaryIdsFinal`. |
| **Abstraction (abstract class)** | `src/com/eventorganizer/models/Event.java:15` | `abstract class Event` with two abstract methods (`getType`, `canInvite`) that every subclass must supply. |
| **Abstraction (abstract class #2)** | `src/com/eventorganizer/models/Notification.java:8` | `abstract class Notification` with abstract `getCategory()`. Four concrete subclasses. |
| **Abstraction (interface)** | `src/com/eventorganizer/interfaces/Reportable.java:3` | `Reportable.generateSummary()` — implemented by both `Event` and `User`, enabling a polymorphic `List<Reportable>`. |
| **Inheritance** | `src/com/eventorganizer/models/PublicEvent.java:7` / `src/com/eventorganizer/models/PrivateEvent.java:7` | Two concrete subclasses extend `Event` and supply their own `canInvite` policy. |
| **Inheritance (notifications)** | `src/com/eventorganizer/models/InvitationNotification.java:3` | One of four concrete `Notification` subclasses (`InvitationNotification`, `EventUpdateNotification`, `RSVPNotification`, `FriendRequestNotification`). |
| **Polymorphism (dispatch)** | `src/com/eventorganizer/services/InvitationService.java:51` | `event.canInvite(creator, invitee)` — the call site does not know whether `event` is a `PublicEvent` or `PrivateEvent`; dispatch is runtime. |
| **Polymorphism (Reportable)** | `src/com/eventorganizer/services/ReportService.java:88` | `generateEventSummary(Reportable r)` works for both `Event` and `User` through the interface. |
| **Composition** | `src/com/eventorganizer/models/Event.java:23` | `List<Invitation> invitations` — an Event is composed of Invitations that live and die with it. |
| **Composition (User)** | `src/com/eventorganizer/models/User.java:29` | `User` aggregates its own `List<Notification>` with capacity enforcement (`enforceNotificationCap`). |
| **Exception hierarchy** | `src/com/eventorganizer/exceptions/AppException.java` | Root unchecked exception with `ErrorCode`; subclasses: `AuthenticationException`, `AuthorizationException`, `ValidationException`, `NotFoundException` (→ `UserNotFoundException`, `EventNotFoundException`, `InvitationNotFoundException`), `ConflictException` (→ `DuplicateUsernameException`, `DuplicateEmailException`, `DuplicateInvitationException`, `DuplicateFriendRequestException`), `InvalidOperationException`, `UnauthorizedException`. |
| **Collections + Streams** | `src/com/eventorganizer/models/User.java:154` | `notifications.stream().filter(n -> !n.isRead()).collect(Collectors.toList())`. |
| **Streams (Event)** | `src/com/eventorganizer/models/Event.java:89` | `invitations.stream().filter(...).count()` in `countByStatus`. |
| **EnumMap (specialized collection)** | `src/com/eventorganizer/services/ReportService.java:41` | `new EnumMap<>(RSVPStatus.class)` keyed by an enum; compact and type-safe. |
| **Enums (exhaustive)** | `src/com/eventorganizer/models/enums/` | Four domain enums: `RSVPStatus`, `EventType`, `EventStatus`, `FriendRequestStatus` (latter includes `WITHDRAWN`). Each has a `parse(String)` helper that throws `ValidationException` on bad input. |
| **Enum-singleton pattern** | `src/com/eventorganizer/store/DataStore.java:19` | `public enum DataStore { INSTANCE; ... }` — safest JVM singleton, serialization-proof. |
| **Generics** | `src/com/eventorganizer/store/DataStore.java:28` | `Map<String, Map<String, Invitation>> invitationIndex` — nested parameterized types. |
| **Comparator composition** | `src/com/eventorganizer/services/EventService.java:27` | `UPCOMING_ASC_BY_NAME` / `PAST_DESC_BY_NAME` — chained comparators. |
| **DTOs (boundary types)** | `src/com/eventorganizer/models/dto/UserActivityReport.java` | Immutable DTOs (`UserProfileDTO`, `EventSummaryReport`, `UserActivityReport`, `BatchInviteResult`) separate the read-model from the domain model. |
| **Composite equality** | `src/com/eventorganizer/models/Invitation.java:63` | `Invitation.equals` uses the composite `(eventId, inviteeId)` rather than the surrogate id. |
| **Optional finders** | `src/com/eventorganizer/store/DataStore.java:60` | `findUserById`, `findUserByUsername`, `findEventById`, `findFriendRequestById`, `findInvitation` all return `Optional<T>` — no nulls leak out. |
| **Clock injection** | `src/com/eventorganizer/store/DataStore.java:37` | `DataStore.INSTANCE.getClock()` — every time-reading call uses it; tests inject a fixed clock via `TestHooks.setClock`. |

## Auxiliary evidence

- **No `System.out.*` in `models/`, `services/`, or `store/`** — verified by inspection; logging goes through `java.util.logging` configured in `utils/LogConfig.java`.
- **Password hygiene** — `char[]` is the primary API (`PasswordHasher.hash(char[], byte[])`), flowing straight from `JPasswordField.getPassword()` into the hasher. Confirm-password comparison uses `MessageDigest.isEqual` (`ui/screens/AuthScreen.java`).
- **Atomicity on composite ops** — `FriendService.sendFriendRequest` / `acceptFriendRequest` and `InvitationService.inviteFriend` use try / rollback around dual-sided mutations.
- **Test coverage** — 68 JUnit 5 tests across 8 files (`test/com/eventorganizer/`).
