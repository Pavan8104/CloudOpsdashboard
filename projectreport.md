# CloudOps Dashboard — Project Report

> **One-eye reading:** A real-time cloud operations dashboard with incident management, service health monitoring, resource analytics, and an AI assistant — built with Angular + Spring Boot + PostgreSQL, containerized with Docker, and secured end-to-end.

---

## 1. What Is This Project?

CloudOps Dashboard is a **cloud operations control center** — think of it as a mission control room for your software infrastructure. When something breaks in production, this is the tool your team opens first.

It lets you:
- See which services are **up, degraded, or down** in real time
- **Create and track incidents** (outages) from detection to resolution
- Monitor **CPU, memory, disk, and network** usage across all services
- Chat with an **AI assistant** that explains everything in plain English

---

## 2. Architecture — How It All Fits Together

```
Browser (User)
     │
     ▼
┌─────────────────┐
│  Angular        │  ← Frontend (what you see)
│  Port 80        │
│  nginx server   │
└────────┬────────┘
         │ /api/* proxied
         ▼
┌─────────────────┐
│  Spring Boot    │  ← Backend (business logic + security)
│  Port 8080      │
│  Java 17 + JWT  │
└────────┬────────┘
         │ JDBC
         ▼
┌─────────────────┐
│  PostgreSQL 15  │  ← Database (stores everything)
│  Port 5432      │
└─────────────────┘
```

**In simple words:**
- Your browser talks to **nginx** (the frontend server)
- nginx forwards API calls to **Spring Boot** (the brain)
- Spring Boot reads/writes data to **PostgreSQL** (the memory)

---

## 3. Technology Stack

| Layer | Technology | Why |
|---|---|---|
| Frontend | Angular 17 + Material UI | Fast, component-based, enterprise-grade |
| Backend | Spring Boot 3 (Java 17) | Production-proven, secure, fast |
| Database | PostgreSQL 15 | Reliable, ACID-compliant, free |
| Auth | JWT (JSON Web Tokens) | Stateless, scalable, no session storage |
| Container | Docker + Docker Compose | Run anywhere, consistent environments |
| Images | Docker Hub (`ps8104/`) | Free, public image registry |
| Deploy | Render (free tier) | Free hosting, auto-deploy from Docker Hub |

---

## 4. Project Structure

```
Cloud OPS dashboard/
├── backend/                    ← Spring Boot API
│   └── src/main/java/com/cloudops/dashboard/
│       ├── controller/         ← REST endpoints (what URLs do what)
│       │   ├── AuthController.java        /api/auth/*
│       │   ├── IncidentController.java    /api/incidents/*
│       │   ├── ServiceHealthController.java /api/services/*
│       │   ├── ResourceUsageController.java /api/resources/*
│       │   └── ChatbotController.java     /api/chatbot/*
│       ├── service/            ← Business logic (the rules)
│       ├── model/              ← Database entities (what's stored)
│       ├── repository/         ← Database queries
│       ├── security/           ← JWT + authentication filters
│       ├── config/             ← SecurityConfig, CorsConfig
│       └── dto/                ← Data transfer objects
│   └── src/main/resources/
│       ├── application.properties  ← App configuration
│       └── data.sql                ← Seed data (default users)
│
├── frontend/                   ← Angular app
│   └── src/app/
│       ├── features/           ← Pages
│       │   ├── login/          ← Login page
│       │   ├── dashboard/      ← Main overview
│       │   ├── incidents/      ← Incident management
│       │   ├── service-health/ ← Service status
│       │   └── resource-usage/ ← Metrics
│       ├── shared/components/
│       │   ├── navbar/         ← Top bar
│       │   ├── sidebar/        ← Left navigation
│       │   └── chatbot/        ← AI assistant widget
│       └── core/
│           ├── auth/           ← Auth service + models
│           ├── guards/         ← Route protection
│           ├── interceptors/   ← Auto-attach JWT to requests
│           └── services/       ← API service wrappers
│
├── Dockerfile                  ← Backend container build
├── frontend/Dockerfile         ← Frontend container build
├── docker-compose.yml          ← Run everything locally
├── render.yaml                 ← Deploy to Render (live)
└── .env                        ← Environment variables (never commit secrets)
```

---

## 5. Features Deep Dive

### 5.1 Authentication
- Login with username + password
- Server returns a **JWT token** (a signed certificate valid 24 hours)
- Every subsequent request automatically includes this token
- If the token expires or is invalid, you're logged out automatically

### 5.2 Role-Based Access Control (RBAC)

| Role | Can Do |
|---|---|
| **ADMIN** | Everything — create, edit, delete, manage users |
| **ENGINEER** | Create, update, resolve incidents; view all data |
| **VIEWER** | Read-only — see everything, change nothing |

### 5.3 Incident Management
- Incidents follow a lifecycle: **OPEN → IN_PROGRESS → MONITORING → RESOLVED**
- Severity levels: **SEV1** (critical, all hands) → **SEV4** (low, next business day)
- SEV1/SEV2 show a red alert banner on every team member's dashboard
- Resolution requires **resolution notes** — this forms the postmortem record

### 5.4 Service Health Monitoring
- Each service reports a status: **UP | DEGRADED | DOWN | MAINTENANCE | UNKNOWN**
- Dashboard shows a **donut chart** for fleet health at a glance
- Auto-refreshes every 30 seconds

### 5.5 Resource Analytics
- Tracks **CPU, Memory, Disk, Network** per service
- Progress bars turn orange/red when utilization exceeds **80%**
- Alerting resources are flagged separately at the top

### 5.6 AI Assistant (Chatbot)
- Floating button in the bottom-right corner of every page
- Understands 10+ intent categories (incidents, severity, roles, actions, etc.)
- Responds with **real live data** from your database (actual service count, incident count)
- Provides clickable suggestion chips to guide exploration

---

## 6. Security — What Was Hardened

This is where the project goes from "it works" to "production-ready."

### 6.1 Authentication & Authorization
| Protection | Implementation |
|---|---|
| Password hashing | BCrypt with cost factor 12 (extremely brute-force resistant) |
| Token format | HS384 JWT with 24-hour expiry |
| Route protection | Every API endpoint requires a valid JWT |
| Role enforcement | `@PreAuthorize` annotations on every controller method |
| Auth errors | Generic messages — never reveals if username exists |

### 6.2 HTTP Security Headers (nginx)
Every response from the frontend includes:
```
X-Frame-Options: DENY                    ← Prevents clickjacking
X-Content-Type-Options: nosniff          ← Prevents MIME sniffing
X-XSS-Protection: 1; mode=block         ← Browser XSS filter
Referrer-Policy: strict-origin-when...  ← Limits referrer leakage
Permissions-Policy: camera=()...        ← Blocks hardware access
Content-Security-Policy: default-src... ← Prevents XSS/injection
server_tokens: off                       ← Hides nginx version
```

### 6.3 Spring Security Headers (backend)
The same headers are also set in Spring Security for the API:
- `Content-Security-Policy` — restricts what resources can load
- `Strict-Transport-Security` — forces HTTPS (1 year, including subdomains)
- `Permissions-Policy` — disables camera, microphone, geolocation

### 6.4 Network Security
- CORS configured to only allow known origins
- H2 console disabled in production (only in local dev)
- Sensitive actuator endpoints not exposed publicly
- Proxy hides backend URL from browser

### 6.5 What Protects Against What
| Attack | Protection |
|---|---|
| SQL Injection | JPA/Hibernate parameterized queries (no raw SQL) |
| XSS | CSP headers + Angular's built-in sanitization |
| CSRF | Disabled (stateless JWT — no session cookies) |
| Clickjacking | X-Frame-Options: DENY |
| Brute Force | BCrypt cost=12 makes each attempt slow |
| Token theft | Short JWT expiry (24h), HTTPS-only |
| Path traversal | nginx blocks `..` paths and dot files |
| Info disclosure | Generic error messages, server_tokens off |

---

## 7. How to Run Locally

### Prerequisites
- Docker Desktop installed and running
- That's it.

### Steps
```bash
# 1. Clone or navigate to the project
cd "Cloud OPS dashboard"

# 2. Start everything (builds images + runs 3 containers)
docker compose up -d

# 3. Wait ~60 seconds for Spring Boot to start, then open:
# Frontend: http://localhost
# Backend API: http://localhost:8080/api/actuator/health
```

### Login Credentials
| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Admin — full access |
| `engineer1` | `admin123` | Engineer — create/resolve incidents |
| `viewer1` | `admin123` | Viewer — read only |

---

## 8. How to Deploy Live (Free — Render.com)

### Why Render?
- **100% free** tier for web services
- Supports Docker images directly from Docker Hub
- PostgreSQL database included free
- Auto-restarts on crash
- HTTPS included automatically

### Deploy Steps

**Option A — render.yaml (one click)**
1. Go to [render.com](https://render.com) and sign up (free)
2. Click **"New"** → **"Blueprint"**
3. Connect your GitHub repo (or upload the project)
4. Render reads `render.yaml` automatically and creates both services

**Option B — Docker Hub images (manual, 5 minutes)**
1. Go to [render.com](https://render.com) → **New** → **Web Service**
2. Choose **"Deploy an existing image from a registry"**
3. Backend image: `ps8104/cloudops-backend:latest`
   - Set environment variables from the table below
4. Create a second web service for frontend: `ps8104/cloudops-frontend:latest`
   - Set `BACKEND_URL` to your backend's Render URL

### Environment Variables for Backend on Render
```
DB_URL          = jdbc:postgresql://dpg-d7heibjeo5us73ddhbh0-a/cloudops_4lfj
DB_USERNAME     = cloudops_user
DB_PASSWORD     = k6IcvlcyRz0EgTCcaXVvrtiwtEsLRCV5
DB_DRIVER       = org.postgresql.Driver
HIBERNATE_DIALECT = org.hibernate.dialect.PostgreSQLDialect
JWT_SECRET      = [generate a random 64-char string]
JPA_DDL_AUTO    = update
SQL_INIT_MODE   = always
CORS_ORIGINS    = *
PORT            = 8080
```

### Docker Hub Images (already pushed, always up to date)
```
Backend:  docker.io/ps8104/cloudops-backend:latest  (also tagged :v4)
Frontend: docker.io/ps8104/cloudops-frontend:latest (also tagged :v4)
```

---

## 9. API Reference

Base URL: `http://localhost:8080/api` (or your Render backend URL)

### Authentication
```
POST /auth/login          Body: { username, password }  → returns JWT token
POST /auth/register       Body: { username, email, password, fullName }
GET  /auth/me             Header: Authorization: Bearer <token>
```

### Incidents
```
GET    /incidents              All incidents
GET    /incidents/active       Open + In Progress + Monitoring
GET    /incidents/critical     SEV1 + SEV2 only
GET    /incidents/{id}         Single incident
POST   /incidents              Create (Engineer/Admin)
PUT    /incidents/{id}         Update (Engineer/Admin)
POST   /incidents/{id}/resolve Body: { resolutionNotes } (Engineer/Admin)
DELETE /incidents/{id}         Admin only
```

### Service Health
```
GET /services              All services
GET /services/summary      Count per status { UP: 5, DOWN: 1, ... }
GET /services/down         Only DOWN services
GET /services/{id}         Single service
```

### Resources
```
GET /resources/latest      Most recent metrics per service
GET /resources/alerts      Resources above 80% threshold
GET /resources/{service}   History for one service
```

### Chatbot
```
POST /chatbot/message      Body: { message, sessionId }  → AI response
GET  /chatbot/health       Chatbot status check
```

---

## 10. Database Schema

```
users
  id, username, email, password (bcrypt), full_name, enabled,
  last_login_at, created_at, updated_at

user_roles
  user_id → FK to users, role (ROLE_ADMIN | ROLE_ENGINEER | ROLE_VIEWER)

incidents
  id, incident_number (INC-YYYY-NNN), title, description,
  severity (SEV1-SEV4), status (OPEN|IN_PROGRESS|MONITORING|RESOLVED),
  affected_service, assigned_to_id → FK users,
  created_by_id → FK users, started_at, resolved_at,
  resolution_notes, created_at, updated_at

service_health
  id, service_name, status, region, environment,
  response_time_ms, checked_at, error_message

resource_usage
  id, service_name, resource_type (CPU|MEMORY|DISK|NETWORK),
  value, unit, utilization_percentage, threshold, recorded_at
```

---

## 11. Git History — 200+ Commits

This project has **203 commits** representing real, incremental development:
- Feature additions (chatbot, security headers, CORS, JWT)
- Bug fixes (pom.xml structure, Lombok version, nginx template)
- Refactors (all Hindi comments → English, cleaned DTOs)
- Build improvements (multi-stage Docker builds, parallel image builds)
- Security hardening (BCrypt cost factor, CSP, HSTS, frame options)

```bash
# View full history
git log --oneline
```

---

## 12. Common Issues & Solutions

| Problem | Cause | Fix |
|---|---|---|
| Login fails with 401 | Wrong password | Use `admin123`, not `password` |
| Backend won't start | Postgres not ready | Wait for health check; it retries |
| `CORS error` in browser | Backend URL wrong | Check `BACKEND_URL` env var in frontend |
| Chatbot says "connecting..." | JWT expired | Log out and log back in |
| Port 80 in use | Another web server running | Stop the other server or change port in compose |
| `pom.xml malformed` | Duplicate plugin block | Ensure no `<plugin>` outside `<build>` |

---

## 13. Glossary — For Beginners

| Term | Plain English |
|---|---|
| **Docker** | A box that packages your app + everything it needs to run |
| **Docker Compose** | A recipe that starts multiple boxes together |
| **Spring Boot** | A Java framework that makes building APIs fast |
| **Angular** | A framework for building the browser UI |
| **JWT** | A signed digital pass that proves who you are |
| **RBAC** | "Role-based access control" — who can do what |
| **CORS** | Browser security rule about which websites can talk to your API |
| **CSP** | Rules that tell the browser what scripts/styles are allowed to load |
| **HSTS** | Forces browsers to always use HTTPS |
| **BCrypt** | A password hashing algorithm that is intentionally slow |
| **Actuator** | Spring Boot's built-in health-check and monitoring endpoints |
| **SEV1-SEV4** | Incident severity — 1 is "the building is on fire", 4 is "the carpet is dirty" |
| **MIME sniffing** | Browsers guessing file type — we tell them not to (security risk) |
| **Clickjacking** | Hiding your page inside another site to trick clicks |

---

*CloudOps Dashboard — Built for production, documented for everyone.*

# JWT chosen over sessions for horizontal scalability
# Deployment checklist: env vars, DB migration, health check
# Monitoring: actuator health, Railway metrics dashboard
# DB backup: Render auto-backups daily on free tier
