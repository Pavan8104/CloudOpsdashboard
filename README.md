# CloudOps Dashboard 🚀

**Enterprise-grade Google Cloud Operations Monitoring Platform**

Yeh project ek full-stack enterprise web application hai jo Google Cloud internal tooling ke liye banaya gaya hai. Service health monitoring, incident tracking, aur resource utilization sab ek jagah.

---

## Architecture Overview

```
┌─────────────────┐    JWT Auth    ┌──────────────────────┐
│  Angular 17     │  ──────────►  │  Spring Boot 3.2     │
│  Frontend       │  ◄──────────  │  REST API Backend    │
│  (Port 4200)    │               │  (Port 8080)         │
└─────────────────┘               └──────────┬───────────┘
                                             │ JPA/Hibernate
                                             ▼
                                  ┌──────────────────────┐
                                  │  PostgreSQL DB       │
                                  │  (Port 5432)         │
                                  └──────────────────────┘
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2, Spring Security, JWT |
| Database | PostgreSQL (prod), H2 (local dev) |
| ORM | JPA/Hibernate |
| Frontend | Angular 17, Angular Material |
| Charts | Chart.js + ng2-charts |
| Auth | JWT (JJWT 0.12.3) |
| DevOps | Docker, docker-compose, GitHub Actions |
| Cloud | GCP Cloud Run, Cloud SQL, GCR |

## Quick Start - Local Development

### Prerequisites
- Java 17+
- Node.js 20+
- Maven 3.8+
- Docker & docker-compose (optional)

### Option 1: Docker Compose (Recommended)

```bash
# Sab kuch ek command se start karo
docker-compose up -d

# Logs dekhne ke liye
docker-compose logs -f backend
```

Frontend: http://localhost:4200
Backend API: http://localhost:8080/api
H2 Console: http://localhost:8080/api/h2-console

### Option 2: Manual Start

**Backend:**
```bash
cd backend
mvn spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm start
```

## Demo Credentials

| Username | Password | Role |
|----------|----------|------|
| admin | password | ADMIN - Full access |
| engineer1 | password | ENGINEER - Ops access |
| viewer1 | password | VIEWER - Read only |

## API Endpoints

| Endpoint | Method | Description | Auth |
|----------|--------|-------------|------|
| `/api/auth/login` | POST | Login, JWT token milega | Public |
| `/api/auth/register` | POST | New user register | Public |
| `/api/services` | GET | All service health | Viewer+ |
| `/api/services/summary` | GET | Status counts | Viewer+ |
| `/api/incidents` | GET | All incidents | Viewer+ |
| `/api/incidents/critical` | GET | SEV1/SEV2 active | Viewer+ |
| `/api/resources/latest` | GET | Latest metrics | Viewer+ |
| `/api/resources/alerts` | GET | Threshold alerts | Viewer+ |

## Role Based Access Control

- **ADMIN**: Full access - create/delete services, incidents, manage users
- **ENGINEER**: Operational access - create/update/resolve incidents, update service status
- **VIEWER**: Read-only - sirf dashboard dekh sakte hain, kuch change nahi kar sakte

## GCP Deployment

```bash
# GCP pe deploy karo
gcloud run deploy cloudops-dashboard \
  --image gcr.io/YOUR_PROJECT/cloudops-dashboard:latest \
  --platform managed \
  --region us-central1 \
  --set-env-vars="GOOGLE_CLOUD_PROJECT=YOUR_PROJECT"
```

Required secrets (GCP Secret Manager mein set karo):
- `cloudops-db-password` - PostgreSQL password
- `cloudops-jwt-secret` - JWT signing secret

## Project Structure

```
CloudOps Dashboard/
├── backend/                    # Spring Boot application
│   └── src/main/java/com/cloudops/dashboard/
│       ├── config/             # Security, CORS config
│       ├── controller/         # REST API controllers
│       ├── dto/                # Data Transfer Objects
│       ├── exception/          # Custom exceptions + global handler
│       ├── model/              # JPA entities
│       ├── repository/         # Spring Data JPA repos
│       ├── security/           # JWT filter + UserDetails
│       └── service/            # Business logic
├── frontend/                   # Angular 17 application
│   └── src/app/
│       ├── core/               # Auth, guards, interceptors, services
│       ├── features/           # Page components (login, dashboard, etc.)
│       └── shared/             # Reusable components + models
├── Dockerfile                  # Multi-stage backend Docker build
├── docker-compose.yml          # Full stack local setup
└── .github/workflows/ci.yml    # GitHub Actions CI/CD
```

---

*Built for Google Cloud Internal Tooling | CloudOps Team*
