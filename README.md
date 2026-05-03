# kronos

An AI-powered daily scheduler. It schedules tasks and builds a time-blocked schedule for whole day using GPT-4o-mini. Built this to learn how to integrate OpenAI APIs into a real Spring Boot app with JWT auth.

Deployed on Railway — needs MySQL and an OpenAI API key.

---

## Stack

- Spring Boot, Java 17
- Spring Security + JWT (stateless auth)
- MySQL + Spring Data JPA
- OpenAI API (gpt-4o-mini)
- Plain HTML + CSS + JS (frontend)

---

## How it works

User signs up → JWT token issued → user chats with Kronos AI in the planner → app sends conversation history to OpenAI with a system prompt that includes the user's day start/end time, break duration, and morning focus preference → AI returns a JSON schedule → user reviews and confirms → tasks saved to DB with PENDING status → statuses auto-update to IN_PROGRESS, COMPLETED, or MISSED based on current time → notifications fire on task start and miss events.

```
POST /api/plans/generate
        ↓
   OpenAIService (sends history + system prompt)
        ↓
   GPT-4o-mini returns JSON {message, tasks[]}
        ↓
POST /api/plans/confirm
        ↓
   DayPlan + Tasks saved to MySQL
        ↓
   Task statuses updated on every fetch
```

Admin can view all users, see plan counts, enable/disable accounts, and delete users.

---

## Pages

- `/index.html` — landing
- `/choose.html` — user or admin
- `/login.html` — user login
- `/dashboard.html` — today's stats and task overview
- `/planner.html` — chat with AI, confirm plan
- `/schedule.html` — view today's schedule, complete tasks
- `/notifications.html` — task reminders and AI suggestions
- `/settings.html` — update profile, day timings, preferences
- `/admin-login.html` — admin login
- `/admin.html` — manage users
- `/admin-settings.html` — admin settings

---

## API

```
POST /api/auth/signup           → register user, returns JWT
POST /api/auth/login            → login, returns JWT
POST /api/auth/admin-login      → admin login, returns JWT

GET  /api/user/profile          → get profile and settings
PUT  /api/user/settings         → update day timings and preferences
PUT  /api/user/change-password  → change password
DELETE /api/user/delete-account → delete account

POST /api/plans/generate        → send message to AI, get schedule back
POST /api/plans/confirm         → save confirmed plan to DB
GET  /api/plans/today           → get today's plan with task statuses
GET  /api/plans/date/{date}     → get plan for a specific date
PUT  /api/plans/regenerate      → regenerate today's plan

PUT  /api/tasks/{id}/complete   → mark task as completed
PUT  /api/tasks/{id}            → update pending task
DELETE /api/tasks/{id}          → delete pending task

GET  /api/dashboard/today-stats   → task counts and productivity %
GET  /api/dashboard/weekly-stats  → last 7 days productivity chart data

GET  /api/notifications           → all notifications
PUT  /api/notifications/{id}/read → mark as read
GET  /api/notifications/unread-count → unread badge count

GET  /api/admin/users             → all users with plan count
PUT  /api/admin/users/{id}/toggle → enable or disable user
DELETE /api/admin/users/{id}      → delete user
GET  /api/admin/stats             → total users plans tasks
```

---

## Run locally

Need MySQL running and an OpenAI API key.

```bash
git clone https://github.com/official-vishalk-1999/Kronos
cd Kronos
```

Set env vars:
```
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/kronos_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=yourpassword
OPENAI_API_KEY=sk-...
```

```bash
./mvnw spring-boot:run
```

Open http://localhost:8080

Default admin login: `admin / admin`

---

## Notes

- AI system prompt includes user's day start/end time and break preference — no need to tell it every time
- Conversation history is sent on every message — AI remembers all constraints from earlier in the chat
- Task statuses (PENDING → IN_PROGRESS → MISSED) update automatically based on current time on every fetch
- Only PENDING tasks can be edited or deleted
- Completing a task fires a success notification
- Missing a task fires a warning notification
- OpenAI response format is forced to JSON — no markdown leaking into the parsed schedule
