# CloudOps Dashboard — Complete Bug Report

**Project:** CloudOps Dashboard  
**Report Date:** 2026-04-18  
**Reporter:** Pavan Sharma  
**Researched on:** ChatGPT, Gemini, Stack Overflow, Spring Security Docs, OWASP  
**Total Bugs Found:** 18  
**Critical:** 6 | High: 5 | Medium: 4 | Low: 3

---

## Quick List — All 18 Bugs At a Glance

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
| BUG-010 | **Real DB credentials hardcoded in `render.yaml`** | 🔴 Critical | ⚠️ Open |
| BUG-011 | **`SHOW_SQL=true` default leaks SQL queries into logs** | 🟠 High | ⚠️ Open |
| BUG-012 | **`SQL_INIT_MODE=always` re-runs seed data on every restart** | 🔴 Critical | ⚠️ Open |
| BUG-013 | **`CORS_ORIGINS=*` in `.env` — any website can call your API** | 🟠 High | ⚠️ Open |
| BUG-014 | **PostgreSQL port 5432 exposed to host — DB accessible without auth** | 🟠 High | ⚠️ Open |
| BUG-015 | **`prometheus` metrics endpoint publicly exposed** | 🟡 Medium | ⚠️ Open |
| BUG-016 | **No rate limiting on login endpoint — brute-force possible** | 🟡 Medium | ⚠️ Open |
| BUG-017 | **Hindi comments remain in `data.sql` — unprofessional, inconsistent** | 🔵 Low | ⚠️ Open |
| BUG-018 | **JWT fallback secret in `application.properties` is predictable** | 🔵 Low | ⚠️ Open |

---

## Detailed Bug Reports

---

### BUG-001 — Malformed `pom.xml`: Orphan `<plugin>` Outside `<build>`

**Severity:** 🔴 Critical | **File:** `backend/pom.xml` line 192 | **Status:** ✅ Fixed

**What happened:**  
A duplicate `<plugin>` block for `maven-compiler-plugin` was pasted **after** the closing `</build>` tag. Maven's XML parser rejected the file entirely. The backend would not build at all — not even a single Java class was compiled.

**Error you saw:**
```
[ERROR] Malformed POM /app/pom.xml: Unrecognised tag: 'plugin'
(position: START_TAG seen ...</build>\n    <plugin>... @192:13)
```

**Root cause:** Copy-paste error. Developer tried to add Lombok version, pasted an entire second `<plugin>` block in the wrong place.

**Fix:** Deleted the orphan block at lines 192–206. Only the misplaced duplicate was removed.

---

### BUG-002 — Lombok `annotationProcessorPath` Missing `<version>`

**Severity:** 🔴 Critical | **File:** `backend/pom.xml` | **Status:** ✅ Fixed

**What happened:**  
After BUG-001 was fixed, build failed again. The Lombok entry inside `<annotationProcessorPaths>` had no `<version>`. Spring Boot's BOM manages versions for `<dependencies>` — but **BOM does NOT apply inside `<annotationProcessorPaths>`**. This is a well-known Maven gotcha.

**Error you saw:**
```
Resolution of annotationProcessorPath dependencies failed:
version can neither be null, empty nor blank
```

**Fix:** Added `<version>1.18.30</version>` explicitly to the Lombok processor path.

---

### BUG-003 — `docker-compose.yml` Pulls Remote Images — Local Code Ignored

**Severity:** 🔴 Critical | **File:** `docker-compose.yml` | **Status:** ✅ Fixed

**What happened:**  
`docker-compose.yml` had `image: ps8104/cloudops-backend:latest` with **no `build:` section**. Running `docker compose up` downloaded old images from Docker Hub. Every local code change was silently ignored — you could edit 1000 lines and the container ran the old version.

**Fix:** Added `build: context:` for both backend and frontend services so Docker builds from local source.

---

### BUG-004 — `${BACKEND_URL}` in nginx Template Never Injected

**Severity:** 🔴 Critical | **File:** `docker-compose.yml` + `frontend/nginx.conf` | **Status:** ✅ Fixed

**What happened:**  
`nginx.conf` uses `proxy_pass ${BACKEND_URL}/api/` — nginx's Docker image processes this as an environment variable template. But `BACKEND_URL` was never set in the `frontend:` service's environment in Compose. Result: nginx started successfully but every `/api/` call failed with a bad gateway.

**Fix:** Added `BACKEND_URL: http://backend:8080` to the frontend service environment in `docker-compose.yml`.

---

### BUG-005 — Login Page Shows Wrong Demo Password

**Severity:** 🟠 High | **File:** `login.component.html` line 104 | **Status:** ✅ Fixed

**What happened:**  
The demo hint on the login page showed `admin / password`. The actual seeded password was `admin123`. Every new user who read the hint got a 401 error with no explanation.

**Proof:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -d '{"username":"admin","password":"password"}'
# → 401 Unauthorized

curl -X POST http://localhost:8080/api/auth/login \
  -d '{"username":"admin","password":"admin123"}'
# → 200 OK ✓
```

**Fix:** Updated hint text from `password` → `admin123`.

---

### BUG-006 — Docker Desktop Not Running

**Severity:** 🟠 High | **File:** Environment | **Status:** ✅ Fixed

**What happened:**  
All Docker commands failed with a socket error. Misleading error — says "is the daemon running?" but gives no instructions.

**Error you saw:**
```
Cannot connect to the Docker daemon at unix:///Users/pavan/.docker/run/docker.sock.
Is the docker daemon running?
```

**Fix:** `open -a Docker` to start Docker Desktop, wait 10 seconds.

---

### BUG-007 — `SecurityConfig` Compile Error: Lambda Chaining Mistake

**Severity:** 🔴 Critical | **File:** `SecurityConfig.java` line 76 | **Status:** ✅ Fixed

**What happened:**  
When adding security headers, `.frameOptions()` was chained off `.permissionsPolicy()`. In Spring Security 6, `permissionsPolicy()` returns a `PermissionsPolicyConfig` object — not the parent `HeadersConfigurer`. So `.frameOptions()` doesn't exist on it.

**Error you saw:**
```
cannot find symbol: method frameOptions() 
location: class HeadersConfigurer.PermissionsPolicyConfig
```

**Fix:** Changed from method-chain style to block style — each header configurer called as a separate statement on `headers`:
```java
.headers(headers -> {
    headers.permissionsPolicy(...);
    headers.frameOptions(...);      // now called on correct object
})
```

---

### BUG-008 — BCrypt Cost Factor Too Low (10)

**Severity:** 🟡 Medium | **File:** `SecurityConfig.java` | **Status:** ✅ Fixed

**What happened:**  
`new BCryptPasswordEncoder()` uses cost factor 10 by default. On a modern GPU, this allows ~100 hash attempts per second. For admin credentials to an ops dashboard, this is insufficient.

**Fix:** Changed to `new BCryptPasswordEncoder(12)` — 4× slower per attempt, same user experience.

---

### BUG-009 — H2 Console `permitAll()` in Production Security Config

**Severity:** 🟡 Medium | **File:** `SecurityConfig.java` | **Status:** ✅ Fixed

**What happened:**  
`SecurityConfig` had `.requestMatchers("/h2-console/**").permitAll()`. If `spring.h2.console.enabled=true` was accidentally set in production, anyone could open an unauthenticated web-based SQL shell to your database.

**Fix:** Removed the H2 console permit from the production filter chain entirely.

---

### BUG-010 — Real Database Credentials Hardcoded in `render.yaml`

**Severity:** 🔴 Critical | **File:** `render.yaml` | **Status:** ⚠️ Open

**What happened:**  
`render.yaml` contains your **live production database password** in plain text:
```yaml
- key: DB_PASSWORD
  value: k6IcvlcyRz0EgTCcaXVvrtiwtEsLRCV5   ← REAL PROD PASSWORD
- key: DB_URL
  value: jdbc:postgresql://dpg-d7heibjeo5us73ddhbh0-a/cloudops_4lfj
```

If this file is ever pushed to a public GitHub repository, your database is immediately accessible to anyone on the internet. GitHub secret scanners and bots actively look for this pattern.

**Why it's dangerous:**  
- Anyone with the URL + username + password can connect directly with `psql` or DBeaver
- Can read all users, incidents, credentials
- Can drop all tables or insert fake data

**Fix (do this now):**
```yaml
# render.yaml — reference secrets from Render's environment group instead
- key: DB_PASSWORD
  fromGroup: cloudops-secrets    ← store real values in Render dashboard, not here
```
Or use Render's **Secret Files** feature, not hardcoded values.

Also: **rotate your database password immediately** if this file was ever committed to git.

---

### BUG-011 — `SHOW_SQL=true` Default Leaks Queries Into Logs

**Severity:** 🟠 High | **File:** `application.properties` | **Status:** ⚠️ Open

**What happened:**  
```properties
spring.jpa.show-sql=${SHOW_SQL:true}   ← defaults to TRUE
```
When `SHOW_SQL` is not set in the environment (e.g., on Render), this defaults to `true`. Every single SQL query — including ones containing user emails, usernames, and incident details — is printed to stdout in plain text. This ends up in your hosting provider's log storage, accessible to anyone with log access.

**What leaks in the logs:**
```sql
select u1_0.username, u1_0.email, u1_0.password from users where u1_0.username=?
```

**Fix:**
```properties
spring.jpa.show-sql=${SHOW_SQL:false}   ← default to false
```

---

### BUG-012 — `SQL_INIT_MODE=always` Re-runs Seed Data on Every Restart

**Severity:** 🔴 Critical | **File:** `.env` + `application.properties` | **Status:** ⚠️ Open

**What happened:**  
Your `.env` file has:
```
SQL_INIT_MODE=always
JPA_DDL_AUTO=update
```

`SQL_INIT_MODE=always` tells Spring Boot to run `data.sql` every single time the application starts. Combined with `JPA_DDL_AUTO=update` (which keeps existing data), this re-inserts the seed rows on every deploy. If the `INSERT` uses `ON CONFLICT DO NOTHING`, it's harmless. If it doesn't, you get **duplicate users, duplicate services, duplicate incidents** on every restart.

**Also dangerous:** `JPA_DDL_AUTO=create-drop` (used in local Compose) **wipes and recreates the entire database** on every container restart — all data lost.

**Fix for production `.env`:**
```
SQL_INIT_MODE=never        ← only run once manually, or use Flyway
JPA_DDL_AUTO=validate      ← validate schema, never modify it
```

---

### BUG-013 — `CORS_ORIGINS=*` Allows Any Website to Call Your API

**Severity:** 🟠 High | **File:** `.env` | **Status:** ⚠️ Open

**What happened:**  
```
CORS_ORIGINS=*
```
This means **any website on the internet** can make authenticated API calls to your backend using a logged-in user's browser session. A malicious site could silently read all your incidents and service data.

**Fix:** Set to your actual frontend URL:
```
CORS_ORIGINS=https://cloudops-frontend.onrender.com
```

---

### BUG-014 — PostgreSQL Port 5432 Exposed to Host Machine

**Severity:** 🟠 High | **File:** `docker-compose.yml` | **Status:** ⚠️ Open

**What happened:**  
```yaml
postgres:
  ports:
    - "5432:5432"   ← maps host port 5432 to container
```
This exposes your PostgreSQL database directly on your Mac's network interface. Anyone on your local network (coffee shop, office) can connect to your database without Docker authentication — just knowing your IP.

**Fix for production:** Remove the `ports:` mapping for postgres entirely. Backend connects via Docker's internal network (`cloudops-network`) — external exposure is unnecessary.
```yaml
postgres:
  # ports: NOT needed — backend reaches it via Docker network
  networks:
    - cloudops-network
```

---

### BUG-015 — `prometheus` Metrics Endpoint Publicly Exposed

**Severity:** 🟡 Medium | **File:** `application.properties` | **Status:** ⚠️ Open

**What happened:**  
```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
```
The `/api/actuator/prometheus` endpoint exposes detailed internal metrics: JVM heap usage, thread counts, HTTP request counts per endpoint, database connection pool stats. This gives attackers a detailed map of your system internals.

**Fix:** Remove `prometheus` from public exposure, or secure it:
```properties
management.endpoints.web.exposure.include=health,info
```
If you need Prometheus, use a separate management port with authentication.

---

### BUG-016 — No Rate Limiting on Login Endpoint — Brute Force Possible

**Severity:** 🟡 Medium | **File:** No file — missing feature | **Status:** ⚠️ Open

**What happened:**  
`/api/auth/login` has no request rate limiting. An attacker can send thousands of login attempts per second. Even with BCrypt cost=12 (~250ms per hash), the server will try every request and eventually exhaust threads.

**Confirmed:** Grep found zero usage of `RateLimiter`, `Bucket4j`, or throttle in the entire codebase.

**Fix:** Add Bucket4j or Spring's built-in rate limiting:
```java
// Add to pom.xml
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
</dependency>
```
Or at nginx level:
```nginx
limit_req_zone $binary_remote_addr zone=login:10m rate=5r/m;
location /api/auth/login {
    limit_req zone=login burst=3 nodelay;
}
```

---

### BUG-017 — Hindi Comments Remain in `data.sql`

**Severity:** 🔵 Low | **File:** `backend/src/main/resources/data.sql` | **Status:** ⚠️ Open

**What happened:**  
While all Java and TypeScript files were cleaned to English, `data.sql` still contains Hindi/Urdu comments:
```sql
-- Yeh file development aur testing ke liye initial data provide karta hai.
-- Production mein yeh mat chalao
-- User roles assign karo - admin ke liye sab roles
-- Real GCP services ka simulation kar rahe hain
```

**Fix:** Replace with English equivalents — minor but important for consistency and professionalism.

---

### BUG-018 — JWT Fallback Secret in `application.properties` Is Predictable

**Severity:** 🔵 Low | **File:** `application.properties` | **Status:** ⚠️ Open

**What happened:**  
```properties
jwt.secret=${JWT_SECRET:CloudOpsSecretKeyForJWTTokenGeneration2024VeryLongAndSecure}
```
If `JWT_SECRET` environment variable is not set, Spring falls back to this hardcoded string. This fallback is now public — it's in the git history. Anyone who knows it can forge valid JWT tokens and impersonate any user, including admins.

**Fix:** Remove the fallback entirely — force an error if the secret is missing:
```properties
jwt.secret=${JWT_SECRET}   ← no fallback — app fails fast if secret missing
```
This is safer than silently using a known-compromised default.

---

## Priority Fix Order

If you have 30 minutes, fix in this order:

```
1. BUG-010 → Remove DB password from render.yaml, rotate credentials
2. BUG-013 → Set CORS_ORIGINS to your actual domain
3. BUG-012 → Change SQL_INIT_MODE=never, JPA_DDL_AUTO=validate in .env
4. BUG-011 → Change SHOW_SQL default to false
5. BUG-014 → Remove postgres ports: from docker-compose.yml
6. BUG-018 → Remove JWT fallback secret
7. BUG-016 → Add nginx rate limiting to /api/auth/login
8. BUG-015 → Remove prometheus from actuator exposure
9. BUG-017 → Clean Hindi from data.sql
```

---

## Verification Commands

```bash
# BUG-010: check render.yaml has no hardcoded secrets
grep -i "password\|secret" render.yaml

# BUG-011: confirm SHOW_SQL is false
grep "SHOW_SQL" backend/src/main/resources/application.properties

# BUG-013: confirm CORS is not wildcard
grep "CORS_ORIGINS" .env

# BUG-014: confirm postgres port not exposed
grep -A3 "postgres:" docker-compose.yml | grep "ports"

# BUG-016: test rate limiting (should block after 5 attempts)
for i in {1..10}; do
  curl -s -o /dev/null -w "%{http_code}\n" \
    -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"wrong"}'
done
```

---

*CloudOps Dashboard Bug Report v2 — 2026-04-18 — 18 bugs total (9 fixed, 9 open)*
# OWASP A01-A10 coverage: see projectreport.md security section
# Pen testing: run OWASP ZAP against staging before prod
# Dependency audit: mvn dependency-check:check for CVEs
# Secret rotation: rotate JWT_SECRET every 90 days
# DB credential rotation: update DB_PASSWORD quarterly
# SSL: Railway auto-provisions Let's Encrypt certificates
# Headers: verify via securityheaders.com after deploy
