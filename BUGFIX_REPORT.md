# CloudOps Dashboard — Bug Fix Report

All bugs encountered and resolved during development, dockerization, and live deployment.

---

## BUG-001 — Docker Daemon Not Running
**Symptom:** `Cannot connect to the Docker daemon at unix:///var/run/docker.sock`
**Root Cause:** Docker Desktop was not started on the Mac.
**Fix:** Launched Docker Desktop via `open -a Docker` and waited for daemon to start.
**Status:** ✅ Fixed

---

## BUG-002 — pom.xml Malformed XML
**Symptom:** `Unrecognised tag: 'plugin' @192:13` — Maven build fails completely.
**Root Cause:** Orphan `<plugin>` block existed outside `</build>` closing tag.
**Fix:** Removed the duplicate plugin block that was placed after `</build>`.
**Status:** ✅ Fixed

---

## BUG-003 — Lombok Version Missing in annotationProcessorPaths
**Symptom:** `version can neither be null, empty nor blank` during Maven compilation.
**Root Cause:** Lombok in `annotationProcessorPaths` does not inherit BOM version — must be explicit.
**Fix:** Added `<version>1.18.30</version>` to the Lombok annotationProcessorPath entry.
**Status:** ✅ Fixed

---

## BUG-004 — Docker Compose Using Old Hub Images Instead of Local Code
**Symptom:** Code changes had no effect — container ran old code.
**Root Cause:** `docker-compose.yml` had no `build:` context, so it pulled stale Docker Hub images.
**Fix:** Added `build: context: .` for backend and `build: context: ./frontend` for frontend.
**Status:** ✅ Fixed

---

## BUG-005 — Frontend Cannot Reach Backend (502 Bad Gateway)
**Symptom:** All API calls failed with 502; nginx could not forward requests.
**Root Cause:** `BACKEND_URL` environment variable was not passed to the frontend container.
**Fix:** Added `BACKEND_URL: http://backend:8080` to frontend service in `docker-compose.yml`.
**Status:** ✅ Fixed

---

## BUG-006 — Wrong Demo Password on Login Page
**Symptom:** Login hint showed `password` but actual password was `admin123` — users could not log in.
**Root Cause:** Demo credentials hint was never updated after password was changed.
**Fix:** Updated login page HTML hint to show `admin123`.
**Status:** ✅ Fixed

---

## BUG-007 — SecurityConfig Compile Error (frameOptions chain)
**Symptom:** `cannot find symbol: frameOptions()` — Spring Boot fails to start.
**Root Cause:** Spring Security 6 requires each header configurer to be a separate statement; method chaining on the builder is not supported.
**Fix:** Changed headers configuration to block-style lambda with individual `headers.frameOptions(...)`, `headers.contentSecurityPolicy(...)` etc. on separate lines.
**Status:** ✅ Fixed

---

## BUG-008 — Hindi/Urdu Comments in Source Code
**Symptom:** Unprofessional codebase; potential confusion for international contributors.
**Root Cause:** All comments were written in Hindi during initial development.
**Fix:** Replaced all Hindi comments across 15+ files with professional English equivalents.
**Status:** ✅ Fixed

---

## BUG-009 — Render.yaml Build Context Mismatch for Frontend
**Symptom:** Render blueprint deployment failed — frontend Dockerfile couldn't find `package.json`.
**Root Cause:** `rootDir: .` in render.yaml sent the repo root as build context, but the frontend Dockerfile expected `frontend/` as root.
**Fix:** Switched render.yaml to use pre-built Docker Hub images (`image: url:`) instead of building from source.
**Status:** ✅ Fixed

---

## BUG-010 — Docker Images Built for ARM (Apple Silicon) Only
**Symptom:** Render rejected image: `invalid platform. Images must be built with platform linux/amd64`.
**Root Cause:** Images were built on Apple Silicon Mac using default `docker build`, which produces `linux/arm64`.
**Fix:** Used `docker buildx build --platform linux/amd64` to produce x86-compatible images, pushed to Docker Hub.
**Status:** ✅ Fixed

---

## BUG-011 — Railway Postgres Internal Hostname Not Resolving
**Symptom:** `java.net.UnknownHostException: postgres.railway.internal` — backend cannot connect to DB.
**Root Cause:** The Railway postgres service was deployed using a custom Docker image, not Railway's native postgres plugin, so internal DNS was not set up.
**Fix:** Switched backend `DB_URL` to use the Render PostgreSQL external hostname which was already running.
**Status:** ✅ Fixed

---

## BUG-012 — Duplicate Seed Data Crashes Backend on Restart
**Symptom:** `duplicate key value violates unique constraint` — Spring Boot crashes on startup after first run.
**Root Cause:** `SQL_INIT_MODE=always` re-runs `data.sql` on every startup, but data already exists from first run.
**Fix:** Changed `SQL_INIT_MODE` to `never` on Railway so seed data only runs on fresh database.
**Status:** ✅ Fixed

---

## BUG-013 — Backend OOM Killed on Railway Free Tier
**Symptom:** `Killed` in deployment logs — container exits immediately after starting.
**Root Cause:** Spring Boot + Hibernate JVM default heap usage exceeded Railway free tier 512MB RAM limit.
**Fix:** Added `JAVA_OPTS=-Xms64m -Xmx384m -XX:+UseContainerSupport` to cap heap within available memory.
**Status:** ✅ Fixed

---

## BUG-014 — CORS 403 Blocking Browser Login
**Symptom:** Browser shows "An unexpected error occurred" — console shows `POST /api/auth/login 403 Forbidden`.
**Root Cause:** `CorsConfig.java` used `setAllowedOrigins("*")` combined with `setAllowCredentials(true)`. The CORS spec forbids wildcard origin with credentials — Spring Security silently rejects all preflight requests.
**Fix:** Changed `setAllowedOrigins()` to `setAllowedOriginPatterns()` which supports wildcard with credentials.
**Status:** ✅ Fixed

---

## BUG-015 — nginx Proxy Cannot Connect to HTTPS Backend
**Symptom:** Login worked via `curl` but failed from browser; 502 errors through nginx proxy.
**Root Cause:** nginx `proxy_pass` to an HTTPS upstream requires `proxy_ssl_server_name on` — without it, TLS SNI handshake fails silently.
**Fix:** Added `proxy_ssl_server_name on; proxy_ssl_verify off;` to the `/api/` proxy block in `nginx.conf`.
**Status:** ✅ Fixed

---

## BUG-016 — Sidebar Hidden Behind Content on Mobile
**Symptom:** On mobile screens, sidebar was either invisible or overlapping content with no way to close.
**Root Cause:** Sidebar used fixed `width: 240px` with no mobile overlay or `translateX` animation; `margin-left` on content conflicted on small screens.
**Fix:** Added `transform: translateX(-100%)` for mobile, `.mobile-open` class toggles it to `translateX(0)`, and a dark overlay closes it on tap. `HostListener` in `AppComponent` detects screen size.
**Status:** ✅ Fixed

---

## BUG-017 — `connect-src 'self'` CSP Blocking API Calls
**Symptom:** Browser blocked API requests due to Content Security Policy.
**Root Cause:** CSP had `connect-src 'self'` which only allows same-origin. When Angular makes HTTPS calls (even through nginx proxy), the strict CSP was too narrow.
**Fix:** Updated CSP to `connect-src 'self' https:` to allow HTTPS API connections.
**Status:** ✅ Fixed

---

## BUG-018 — Prometheus Exposed Publicly via Actuator
**Symptom:** `/api/actuator/prometheus` was publicly accessible, exposing internal metrics.
**Root Cause:** `management.endpoints.web.exposure.include=health,info,metrics,prometheus` in `application.properties`.
**Fix:** Removed `metrics` and `prometheus` — only `health` and `info` are now exposed.
**Status:** ✅ Fixed

---

## BUG-019 — SHOW_SQL Defaulting to True (Leaking Queries to Logs)
**Symptom:** All SQL queries printed to production logs, exposing schema structure.
**Root Cause:** `spring.jpa.show-sql=${SHOW_SQL:true}` defaulted to `true` when env var not set.
**Fix:** Changed default to `false` — SQL logging now requires explicit `SHOW_SQL=true`.
**Status:** ✅ Fixed

---

## BUG-020 — H2 Console Enabled by Default
**Symptom:** `/api/h2-console` was accessible — potential data exposure in misconfigured environments.
**Root Cause:** `spring.h2.console.enabled=true` hardcoded in `application.properties`.
**Fix:** Changed to `${H2_CONSOLE_ENABLED:false}` — disabled unless explicitly enabled via env var.
**Status:** ✅ Fixed

---

*CloudOps Dashboard — 20 bugs found, 20 bugs fixed. Production-grade.*
