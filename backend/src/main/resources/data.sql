-- CloudOps Dashboard - Seed Data
-- This file provides initial data for development and testing.
-- Spring Boot runs this automatically on startup (with H2).
-- Do not run this in production - use a proper migration tool like Flyway/Liquibase.

-- =====================================================
-- DEFAULT ADMIN USER
-- Password: admin123 (BCrypt encoded)
-- IMPORTANT: Change this immediately in production!
-- =====================================================
INSERT INTO users (username, email, password, full_name, enabled, created_at, updated_at)
VALUES (
    'admin',
    'admin@cloudops.internal',
    '$2b$10$iuvciBocxQLcvv7yXeYHUuzZvYT23LOic3bsc79hcoL4DCB0GqMt6',
    'CloudOps Admin',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT DO NOTHING;

-- Engineer user for demo purposes
INSERT INTO users (username, email, password, full_name, enabled, created_at, updated_at)
VALUES (
    'engineer1',
    'pavan@cloudops.internal',
    '$2b$10$iuvciBocxQLcvv7yXeYHUuzZvYT23LOic3bsc79hcoL4DCB0GqMt6',
    'Pavan Kumar (Lead SRE)',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT DO NOTHING;

-- Viewer user for read-only access demo
INSERT INTO users (username, email, password, full_name, enabled, created_at, updated_at)
VALUES (
    'viewer1',
    'viewer@cloudops.internal',
    '$2b$10$iuvciBocxQLcvv7yXeYHUuzZvYT23LOic3bsc79hcoL4DCB0GqMt6',
    'Priya Sharma (Product)',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT DO NOTHING;

-- Assign user roles - Admin gets all roles, Engineer gets Engineer role
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN' FROM users WHERE username = 'admin' ON CONFLICT DO NOTHING;
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ENGINEER' FROM users WHERE username = 'engineer1' ON CONFLICT DO NOTHING;
INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_VIEWER' FROM users WHERE username = 'viewer1' ON CONFLICT DO NOTHING;

-- =====================================================
-- SAMPLE SERVICES - For dashboard visualization
-- Simulating real cloud services
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
-- SAMPLE INCIDENTS - For incident history demo
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
        CURRENT_TIMESTAMP - INTERVAL '5 hours',
        CURRENT_TIMESTAMP - INTERVAL '5 hours',
        CURRENT_TIMESTAMP - INTERVAL '2 hours'
    );
