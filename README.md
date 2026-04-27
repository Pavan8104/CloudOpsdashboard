# CloudOps Dashboard
live link will exoire due to credit exhaust link : https://cloudops-frontend-production.up.railway.app/login
**Enterprise-grade Cloud Operations Monitoring Platform**

A full-stack operations dashboard for real-time service health monitoring, incident tracking, resource utilization analytics, and a smart assistant.

---

## Live Links

| Service | Link |
|---|---|
| Frontend (Docker Hub) | https://hub.docker.com/r/ps8104/cloudops-frontend |
| Backend (Docker Hub) | https://hub.docker.com/r/ps8104/cloudops-backend |
| Deploy on Render | https://render.com |

### Deploy in One Command
```bash
docker compose up -d
# Frontend: http://localhost
# Backend:  http://localhost:8080/api
```

---

## Architecture

```
┌─────────────────┐    JWT Auth    ┌──────────────────────┐
│  Angular 17     │  ──────────►  │  Spring Boot 3.2     │
│  Frontend       │  ◄──────────  │  REST API Backend    │
│  Port 80        │               │  Port 8080           │
└─────────────────┘               └──────────┬───────────┘
                                             │ JPA/Hibernate
                                             ▼
                                  ┌──────────────────────┐
                                  │  PostgreSQL 15       │
                                  │  Port 5432           │
                                  └──────────────────────┘
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Angular 17, Angular Material, Chart.js |
| Backend | Java 21, Spring Boot 3.2, Spring Security |
| Database | PostgreSQL 15 (prod), H2 (local dev) |
| Auth | JWT (JJWT 0.12.3), BCrypt cost=12 |
| DevOps | Docker, Docker Compose, GitHub Actions |
| Deploy | Render.com (free tier) |

---

## Docker Hub Images

| Image | Tags | Link |
|---|---|---|
| Backend | `latest`, `v4` | https://hub.docker.com/r/ps8104/cloudops-backend |
| Frontend | `latest`, `v4` | https://hub.docker.com/r/ps8104/cloudops-frontend |

Pull manually:
```bash
docker pull ps8104/cloudops-backend:latest
docker pull ps8104/cloudops-frontend:latest
```

---

## Quick Start (Local)

```bash
docker compose up -d
```

Wait ~60 seconds for Spring Boot, then open http://localhost

---

## Demo Credentials

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Admin — full access |
| `engineer1` | `admin123` | Engineer — create/resolve incidents |
| `viewer1` | `admin123` | Viewer — read only |

---

## Deploy Live (Free — Render.com)

1. Go to https://render.com and sign up free
2. **New → Web Service → Deploy existing image**
3. Backend image: `ps8104/cloudops-backend:latest`
4. Frontend image: `ps8104/cloudops-frontend:latest`
5. Set environment variables from `.env` file
6. Your public URL: `https://your-app.onrender.com`

Full deploy guide: see `projectreport.md`

---

## API Endpoints

| Endpoint | Method | Auth | Description |
|---|---|---|---|
| `/api/auth/login` | POST | Public | Login, returns JWT |
| `/api/auth/register` | POST | Public | Register new user |
| `/api/services` | GET | Viewer+ | All service health |
| `/api/services/summary` | GET | Viewer+ | Status counts |
| `/api/incidents` | GET | Viewer+ | All incidents |
| `/api/incidents/critical` | GET | Viewer+ | SEV1/SEV2 active |
| `/api/resources/latest` | GET | Viewer+ | Latest metrics |
| `/api/resources/alerts` | GET | Viewer+ | Threshold alerts |
| `/api/chatbot/message` | POST | Viewer+ | Smart assistant |

---

## Roles

| Role | Permissions |
|---|---|
| **ADMIN** | Full access — create, edit, delete, manage users |
| **ENGINEER** | Create, update, resolve incidents; view all data |
| **VIEWER** | Read-only — view everything, change nothing |

---

## Project Structure

```
Cloud OPS dashboard/
├── backend/                    # Spring Boot API
│   └── src/main/java/com/cloudops/dashboard/
│       ├── config/             # SecurityConfig, CorsConfig
│       ├── controller/         # REST endpoints
│       ├── service/            # Business logic + ChatbotService
│       ├── model/              # JPA entities
│       ├── repository/         # Database queries
│       └── security/           # JWT filter, token provider
├── frontend/                   # Angular 17 app
│   └── src/app/
│       ├── features/           # Pages: login, dashboard, incidents
│       └── shared/components/  # Navbar, sidebar, chatbot
├── Dockerfile                  # Backend multi-stage build
├── frontend/Dockerfile         # Frontend multi-stage build
├── docker-compose.yml          # Local full-stack setup
├── render.yaml                 # Render.com deploy blueprint
├── projectreport.md            # Full project documentation
└── BUGREPORT.md                # 18 bugs found and documented
```

---

*CloudOps Dashboard v1.0 — 210 commits — Secured, Containerized, Production-Ready*

## 🛡️ Production Security Checklist
- [x] **JWT Secret Strength**: Validation implemented in `JwtTokenProvider`.
- [x] **Rate Limiting**: Brute force protection on Login and Registration.
- [x] **Security Headers**: Tightened CSP, HSTS, and XSS headers in `SecurityConfig`.
- [x] **Input Validation**: Strict constraints on DTOs and Controller endpoints.
- [ ] **Secrets Management**: Use environment variables (configured in `application.properties`).
- [ ] **HTTPS**: Must be enforced at the hosting provider (Render/GCP).

# 
