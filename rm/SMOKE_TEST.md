# Smoke Test — 10 Steps

Run after a clean build. Exercises every spec feature through the Swing UI.
Seeded users: `alice` / `alice123`, `bob` / `bob12345`, `carol` / `carol123`.

| # | Step | Expected result |
|---|---|---|
| 1 | Launch `./run.sh` (or `run.bat`). Log in as `alice` / `alice123`. | Auth screen disappears; dashboard loads; status bar shows `Welcome, alice` and an unread-count badge. |
| 2 | Dashboard → My Events. | `Community Meetup` and `Alice's Housewarming` visible under Upcoming. |
| 3 | Sidebar → Notifications, double-click the invitation from bob. | Deep-links to the event; the notification becomes read; badge decrements. |
| 4 | My Events → **Create Event**. Name `Birthday Party`, date = 7 days from today, type `Private`. Save. | Toast `Event created`; row appears under Upcoming. |
| 5 | Open **Birthday Party** → **Invite Friends** → select `bob`, `carol` → Invite. | Toast `2 invited`. Dialog closes. |
| 6 | Log out. Log in as `bob` / `bob12345`. | Badge shows at least one new notification. |
| 7 | Notifications → double-click the invitation → **Accept**. | Toast `RSVP saved`. Invitation list shows `Accepted`. |
| 8 | Log out. Log in as `alice`. Notifications → bob's RSVP visible. Open **Birthday Party** → RSVP summary shows `Accepted: 1, Pending: 1`. | Counts match. |
| 9 | Edit **Birthday Party** (shift date +1 day). | Bob receives an `event updated` notification; a rapid second edit coalesces into the same row (within 60 s). |
| 10 | Cancel **Birthday Party** with reason `venue unavailable`. Reports panel shows updated KPIs. | Bob's feed carries the cancellation + reason. Reports KPIs: Total/Upcoming/Past/Attendees consistent. |

**Pass criteria**: every step completes without an uncaught exception bubbling to the UI. All toasts render on the glass pane; all destructive actions prompt via `ConfirmDialog`; every input field validates before submission; password inputs are masked throughout.
