-- CloudOps Dashboard - Seed Data
-- Yeh file development aur testing ke liye initial data provide karta hai.
-- Spring Boot automatically yeh run karta hai jab application start hoti hai (H2 ke saath).
-- Production mein yeh mat chalao - proper migration tool use karo (Flyway/Liquibase).

-- =====================================================
-- DEFAULT ADMIN USER
-- Password: admin123 (BCrypt encoded)
-- IMPORTANT: Production mein yeh change karo turant!
-- BCrypt hash of "admin123" - online generator se verify kar sakte ho
-- =====================================================
INSERT INTO users (username, email, password, full_name, enabled, created_at, updated_at)
VALUES (
    'admin',
    'admin@cloudops.internal',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    'CloudOps Admin',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT DO NOTHING;

-- Engineer user - demo ke liye
INSERT INTO users (username, email, password, full_name, enabled, created_at, updated_at)
VALUES (
    'engineer1',
    'engineer@cloudops.internal',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    'Rahul Kumar (SRE)',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT DO NOTHING;

-- Viewer user - read-only access demo ke liye
INSERT INTO users (username, email, password, full_name, enabled, created_at, updated_at)
VALUES (
    'viewer1',
    'viewer@cloudops.internal',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    'Priya Sharma (Product)',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT DO NOTHING;

-- User roles assign karo - admin ke liye sab roles, engineer ke liye sirf engineer
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN' FROM users WHERE username = 'admin' ON CONFLICT DO NOTHING;
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ENGINEER' FROM users WHERE username = 'engineer1' ON CONFLICT DO NOTHING;
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_VIEWER' FROM users WHERE username = 'viewer1' ON CONFLICT DO NOTHING;

-- =====================================================
-- SAMPLE GCP SERVICES - Dashboard mein dikhne ke liye
-- Real GCP services ka simulation kar rahe hain
-- =====================================================

INSERT INTO service_health (service_name, service_type, status, region, gcp_project_id, response_time_ms, uptime_percentage, status_message, last_checked_at, created_at, updated_at)
VALUES
    ('GKE Production Cluster', 'Compute', 'UP', 'us-central1', 'cloudops-prod', 45, 99.95, 'All nodes healthy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Cloud SQL Primary', 'Database', 'UP', 'us-central1', 'cloudops-prod', 12, 99.99, 'Replication lag: 0ms', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Cloud Run API Gateway', 'Serverless', 'UP', 'us-central1', 'cloudops-prod', 89, 99.9, 'Serving traffic normally', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Pub/Sub Orders Topic', 'Messaging', 'DEGRADED', 'us-east1', 'cloudops-prod', 234, 98.5, 'High message backlog - processing delayed', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Cloud Storage Bucket', 'Storage', 'UP', 'us-central1', 'cloudops-prod', 56, 100.0, 'Normal operations', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('VPC Load Balancer', 'Networking', 'UP', 'global', 'cloudops-prod', 23, 99.98, 'All backends healthy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Cloud Memorystore Redis', 'Cache', 'DOWN', 'asia-south1', 'cloudops-staging', 0, 95.2, 'Connection refused - node restarting', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('BigQuery Analytics', 'Analytics', 'UP', 'us-central1', 'cloudops-prod', 145, 99.7, 'Query processing normal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- =====================================================
-- SAMPLE INCIDENTS - Incident history demo ke liye
-- =====================================================

INSERT INTO incidents (title, description, severity, status, incident_number, started_at, created_at, updated_at)
VALUES
    (
        'Cloud Memorystore Redis Down - Asia South1',
        'Redis instance in asia-south1 is not accepting connections. All cache misses going to Cloud SQL causing increased latency.',
        'SEV2',
        'IN_PROGRESS',
        'INC-20240117-0001',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'Pub/Sub Message Backlog Growing - Orders Topic',
        'Message backlog in orders topic exceeded 10k messages. Consumer group lag increasing. Order processing delays expected.',
        'SEV3',
        'OPEN',
        'INC-20240117-0002',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'GKE Node Pool Auto-scaling Event',
        'Node pool scaled from 5 to 8 nodes due to high CPU. Normal traffic spike during sale event. No action required.',
        'SEV4',
        'RESOLVED',
        'INC-20240116-0001',
        DATEADD(HOUR, -5, CURRENT_TIMESTAMP),
        DATEADD(HOUR, -5, CURRENT_TIMESTAMP),
        DATEADD(HOUR, -2, CURRENT_TIMESTAMP)
    );
