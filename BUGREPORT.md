# CloudOps Dashboard — Complete Bug Report

**Project:** CloudOps Dashboard  
**Report Date:** 2026-04-24  
**Reporter:** Pavan Sharma  
**Researched on:** Stack Overflow, Spring Security Docs, OWASP  
**Total Bugs Found:** 25  
**Critical:** 10 | High: 8 | Medium: 4 | Low: 3

---

## Quick List — All 25 Bugs At a Glance

| # | Bug | Severity | Status |
|---|---|---|---|
| BUG-001 | `pom.xml` — orphan `<plugin>` block breaks Maven parse | 🔴 Critical | ✅ Fixed |
| BUG-002 | Lombok `annotationProcessorPath` has no `<version>` | 🔴 Critical | ✅ Fixed |
| BUG-003 | `docker-compose.yml` pulls remote images — local code never built | 🔴 Critical | ✅ Fixed |
| BUG-004 | `${BACKEND_URL}` in nginx template never injected — all API calls 502 | 🔴 Critical | ✅ Fixed |
| BUG-005 | Login hint shows wrong password (`password` vs `admin123`) | 🟠 High | ✅ Fixed |
| BUG-006 | Docker Desktop not running — cryptic socket error | 🟠 High | ✅ Fixed |
| BUG-007 | `SecurityConfig` compile error — `frameOptions()` on wrong lambda object | 🔴 Critical | ✅ Fixed |
| BUG-008 | BCrypt cost factor 10 — too weak for production | 🟡 Medium | ✅ Fixed |
| BUG-009 | H2 console `permitAll()` still in production security config | 🟡 Medium | ✅ Fixed |
| BUG-010 | Real DB credentials hardcoded in `render.yaml` | 🔴 Critical | ✅ Fixed |
| BUG-011 | `SHOW_SQL=true` default leaks SQL queries into logs | 🟠 High | ✅ Fixed |
| BUG-012 | `SQL_INIT_MODE=always` re-runs seed data on every restart | 🔴 Critical | ✅ Fixed |
| BUG-013 | `CORS_ORIGINS=*` — any website can call your API | 🟠 High | ✅ Fixed |
| BUG-014 | PostgreSQL port 5432 exposed to host — DB accessible without auth | 🟠 High | ✅ Fixed |
| BUG-015 | `prometheus` metrics endpoint publicly exposed | 🟡 Medium | ✅ Fixed |
| BUG-016 | No rate limiting on login endpoint — brute-force possible | 🟡 Medium | ✅ Fixed |
| BUG-017 | Hindi comments remain in `data.sql` — unprofessional, inconsistent | 🔵 Low | ✅ Fixed |
| BUG-018 | JWT fallback secret in `application.properties` is predictable | 🔵 Low | ✅ Fixed |
| BUG-019 | Hardcoded creator ID `1L` in incident creation | 🔴 Critical | ✅ Fixed |
| BUG-020 | Nginx DNS resolution failure (Intermittent 502/DNS error) | 🟠 High | ✅ Fixed |
| BUG-021 | Dashboard N+1 API problem (6 calls to load one page) | 🟠 High | ✅ Fixed |
| BUG-022 | Non-root container execution missing (Red Hat standard) | 🔴 Critical | ✅ Fixed |
| BUG-023 | Missing automated library vulnerability scanning (CVEs) | 🟠 High | ✅ Fixed |
| BUG-024 | Loading state mismatch in HTML (Consolidation build error) | 🔴 Critical | ✅ Fixed |
| BUG-025 | Non-professional "AI/ChatGPT" traces in codebase | 🔵 Low | ✅ Fixed |

---

## Detailed Bug Reports (Recent Additions)

---

### BUG-019 — Hardcoded Creator ID in Incident Creation

**Severity:** 🔴 Critical | **Status:** ✅ Fixed

**What happened:**  
In `IncidentController.java`, the `createIncident` method was hardcoded to assign `1L` as the creator's user ID for every new incident. This broke accountability and audit trails, as every incident appeared to be created by the first user in the database, regardless of who was actually logged in.

**Fix:**  
Updated `IncidentService` and `IncidentController` to extract the username from the `Authentication` principal and fetch the actual user record from the database.

---

### BUG-020 — Nginx DNS Resolution Failure (Intermittent 502)

**Severity:** 🟠 High | **Status:** ✅ Fixed

**What happened:**  
Nginx was failing to re-resolve the backend IP when Render rotated service instances, leading to "DNS not found" or "Bad Gateway" errors. Nginx only resolves variable-based upstream names once at startup by default.

**Fix:**  
Added a dedicated `resolver` directive (8.8.8.8) with a short TTL (30s) to `nginx.conf`, ensuring Nginx always finds the latest backend instance.

---

### BUG-021 — Dashboard N+1 API Problem (6 Calls to Load One Page)

**Severity:** 🟠 High | **Status:** ✅ Fixed

**What happened:**  
The Angular dashboard was making 6 separate API requests (Services, Summary, Incidents, Critical, Resources, Alerts) on every refresh. This caused high latency, flickering UI, and unnecessary server load.

**Fix:**  
Implemented a consolidated `DashboardController` and `DashboardSummaryDTO` that returns all data in a single "Super Endpoint" call.

---

### BUG-022 — Non-Root Container Execution (Enterprise Security)

**Severity:** 🔴 Critical | **Status:** ✅ Fixed

**What happened:**  
Containers were running as `root` by default, violating enterprise security standards (Red Hat/OpenShift). A compromised container could potentially gain control of the host machine.

**Fix:**  
Switched to Red Hat Universal Base Images (UBI 8) and configured non-privileged users (`185` for Java, `1001` for Nginx).

---

### BUG-025 — AI Traces in Professional Codebase

**Severity:** 🔵 Low | **Status:** ✅ Fixed

**What happened:**  
Multiple references to "AI", "ChatGPT", "Gemini", and "Claude" were scattered across docs and UI strings, which is unprofessional for a production tool.

**Fix:**  
Thoroughly scrubbed the project and rebranded everything to "Smart Assistant" or "Smart Integration."

---

*CloudOps Dashboard Bug Report v3 — 2026-04-24 — 25 bugs total (25 fixed, 0 open)*
