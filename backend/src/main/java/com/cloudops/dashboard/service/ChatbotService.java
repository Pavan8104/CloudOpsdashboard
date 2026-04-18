package com.cloudops.dashboard.service;

import com.cloudops.dashboard.dto.ChatRequest;
import com.cloudops.dashboard.dto.ChatResponse;
import com.cloudops.dashboard.repository.IncidentRepository;
import com.cloudops.dashboard.repository.ServiceHealthRepository;
import com.cloudops.dashboard.repository.ResourceUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final IncidentRepository incidentRepository;
    private final ServiceHealthRepository serviceHealthRepository;
    private final ResourceUsageRepository resourceUsageRepository;

    private static final Map<Pattern, String> INTENT_PATTERNS = new LinkedHashMap<>();

    static {
        INTENT_PATTERNS.put(Pattern.compile("(?i)(incident|incidents|outage|down|alert|critical|sev)"), "incidents");
        INTENT_PATTERNS.put(Pattern.compile("(?i)(service|services|health|status|monitor|uptime)"), "services");
        INTENT_PATTERNS.put(Pattern.compile("(?i)(resource|cpu|memory|disk|usage|metric|utilization|capacity)"), "resources");
        INTENT_PATTERNS.put(Pattern.compile("(?i)(how|what|explain|help|guide|understand|learn|show|tell)"), "help");
        INTENT_PATTERNS.put(Pattern.compile("(?i)(dashboard|overview|summary|home|main)"), "dashboard");
        INTENT_PATTERNS.put(Pattern.compile("(?i)(role|permission|admin|engineer|viewer|access|login|auth)"), "roles");
        INTENT_PATTERNS.put(Pattern.compile("(?i)(resolve|fix|close|acknowledge|assign|escalate)"), "actions");
        INTENT_PATTERNS.put(Pattern.compile("(?i)(sev1|sev2|severity|priority|p1|p2|p3|p4)"), "severity");
        INTENT_PATTERNS.put(Pattern.compile("(?i)(report|export|history|log|audit|past|previous)"), "history");
        INTENT_PATTERNS.put(Pattern.compile("(?i)(hello|hi|hey|start|begin|greet)"), "greeting");
    }

    public ChatResponse process(ChatRequest request) {
        String message = request.getMessage().trim();
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();
        String intent = detectIntent(message);

        log.debug("Chatbot processing message with intent: {}", intent);

        String responseMessage = generateResponse(intent, message);
        List<String> suggestions = generateSuggestions(intent);

        return ChatResponse.builder()
                .message(responseMessage)
                .sessionId(sessionId)
                .suggestions(suggestions)
                .timestamp(Instant.now())
                .intent(intent)
                .build();
    }

    private String detectIntent(String message) {
        for (Map.Entry<Pattern, String> entry : INTENT_PATTERNS.entrySet()) {
            if (entry.getKey().matcher(message).find()) {
                return entry.getValue();
            }
        }
        return "general";
    }

    private String generateResponse(String intent, String message) {
        return switch (intent) {
            case "greeting" -> buildGreetingResponse();
            case "incidents" -> buildIncidentResponse(message);
            case "services" -> buildServiceResponse();
            case "resources" -> buildResourceResponse();
            case "dashboard" -> buildDashboardResponse();
            case "roles" -> buildRolesResponse();
            case "actions" -> buildActionsResponse(message);
            case "severity" -> buildSeverityResponse();
            case "history" -> buildHistoryResponse();
            case "help" -> buildHelpResponse(message);
            default -> buildGeneralResponse();
        };
    }

    private String buildGreetingResponse() {
        long activeIncidents = incidentRepository.count();
        long services = serviceHealthRepository.count();
        return String.format(
            "👋 Welcome to CloudOps Assistant! I'm here to help you navigate and understand this operations dashboard.\n\n" +
            "**Current System Snapshot:**\n" +
            "• %d services being monitored\n" +
            "• %d total incidents in the system\n\n" +
            "I can help you with:\n" +
            "• Understanding incidents and their severity levels\n" +
            "• Navigating service health monitoring\n" +
            "• Managing resource utilization alerts\n" +
            "• Explaining roles and permissions\n\n" +
            "What would you like to know?",
            services, activeIncidents
        );
    }

    private String buildIncidentResponse(String message) {
        long total = incidentRepository.count();
        if (message.toLowerCase().contains("create") || message.toLowerCase().contains("new")) {
            return "**Creating an Incident**\n\n" +
                "To create a new incident:\n" +
                "1. Navigate to the **Incidents** section in the sidebar\n" +
                "2. Click the **+ New Incident** button (requires Engineer or Admin role)\n" +
                "3. Fill in the required fields:\n" +
                "   - **Title**: Clear, concise description of the problem\n" +
                "   - **Severity**: SEV1 (Critical) → SEV4 (Low)\n" +
                "   - **Affected Service**: Which service is impacted\n" +
                "   - **Description**: Detailed impact and symptoms\n" +
                "4. Assign to an on-call engineer if known\n\n" +
                "The system will auto-generate an incident number (e.g., INC-2024-001).";
        }
        if (message.toLowerCase().contains("resolve") || message.toLowerCase().contains("close")) {
            return "**Resolving an Incident**\n\n" +
                "To resolve an incident:\n" +
                "1. Open the incident from the **Incidents** list\n" +
                "2. Click **Resolve Incident** button\n" +
                "3. Enter detailed **Resolution Notes** — this is required for the postmortem record\n" +
                "4. The incident status will change to **RESOLVED**\n\n" +
                "⚠️ Note: Resolution notes are mandatory. A good postmortem captures: root cause, timeline, and preventive actions.";
        }
        return String.format(
            "**Incident Management Overview**\n\n" +
            "The system currently tracks **%d incidents** total.\n\n" +
            "**Incident Lifecycle:**\n" +
            "OPEN → IN_PROGRESS → MONITORING → RESOLVED\n\n" +
            "**Key Concepts:**\n" +
            "• **Active incidents** include OPEN, IN_PROGRESS, and MONITORING states\n" +
            "• **Critical incidents** are SEV1 and SEV2 — shown as a red banner on the dashboard\n" +
            "• **Incident numbers** are auto-generated in format INC-YYYY-NNN\n" +
            "• Only **Engineers** and **Admins** can create or resolve incidents\n\n" +
            "Ask me about: creating incidents, severity levels, or resolving incidents.",
            total
        );
    }

    private String buildServiceResponse() {
        long total = serviceHealthRepository.count();
        return String.format(
            "**Service Health Monitoring**\n\n" +
            "Currently monitoring **%d services** across your infrastructure.\n\n" +
            "**Status Levels:**\n" +
            "🟢 **UP** — Service is operating normally\n" +
            "🟡 **DEGRADED** — Partial outage or reduced performance\n" +
            "🔴 **DOWN** — Service is completely unavailable\n" +
            "🔵 **MAINTENANCE** — Planned downtime window\n" +
            "⚪ **UNKNOWN** — Health check not reachable\n\n" +
            "**Reading the Dashboard:**\n" +
            "• The donut chart shows your overall fleet health at a glance\n" +
            "• Each service card shows: status, region, and response time\n" +
            "• Services are grouped by environment (prod/staging/dev)\n\n" +
            "Navigate to **Service Health** in the sidebar for the full list.",
            total
        );
    }

    private String buildResourceResponse() {
        long total = resourceUsageRepository.count();
        return String.format(
            "**Resource Utilization Monitoring**\n\n" +
            "Tracking **%d resource metrics** across all services.\n\n" +
            "**Monitored Resource Types:**\n" +
            "• **CPU** — Processor utilization percentage\n" +
            "• **Memory** — RAM consumption percentage\n" +
            "• **Disk** — Storage capacity used\n" +
            "• **Network** — Bandwidth utilization\n\n" +
            "**Alert Thresholds:**\n" +
            "• Resources exceeding **80%%** utilization trigger an alert\n" +
            "• Alerting resources appear at the top of the resource dashboard\n" +
            "• The dashboard shows a count of active resource alerts in the header\n\n" +
            "**Pro Tips:**\n" +
            "• Progress bars turn **orange/red** when approaching critical thresholds\n" +
            "• Historical trends help you forecast capacity needs\n" +
            "• Correlate resource spikes with active incidents for root cause analysis",
            total
        );
    }

    private String buildDashboardResponse() {
        return "**Navigating the Operations Dashboard**\n\n" +
            "The dashboard is your command center — it gives you a real-time overview of your entire infrastructure.\n\n" +
            "**Dashboard Sections:**\n\n" +
            "📊 **Summary Cards** (top row)\n" +
            "   - Total Services monitored\n" +
            "   - Services currently down\n" +
            "   - Active incident count\n" +
            "   - Resource alerts firing\n\n" +
            "🚨 **Critical Alert Banner**\n" +
            "   - Appears automatically when SEV1/SEV2 incidents are active\n" +
            "   - Click to jump directly to the incidents list\n\n" +
            "📈 **Service Health Panel** (left column)\n" +
            "   - Donut chart showing fleet health distribution\n" +
            "   - List of your services with current status\n\n" +
            "⚡ **Resource Utilization Panel** (right column)\n" +
            "   - Real-time resource metrics with progress bars\n" +
            "   - Highlights any resources over threshold\n\n" +
            "The dashboard **auto-refreshes every 30 seconds** — you can also manually refresh using the button.";
    }

    private String buildRolesResponse() {
        return "**Roles and Permissions**\n\n" +
            "CloudOps Dashboard uses role-based access control (RBAC) with three roles:\n\n" +
            "👑 **ADMIN**\n" +
            "   - Full access to all features\n" +
            "   - Can create, update, and delete incidents\n" +
            "   - Can manage users and system settings\n" +
            "   - Can access all reports and audit logs\n\n" +
            "🔧 **ENGINEER**\n" +
            "   - Can create and resolve incidents\n" +
            "   - Can update incident status and assign ownership\n" +
            "   - Read access to all monitoring data\n" +
            "   - Cannot delete incidents or manage users\n\n" +
            "👁️ **VIEWER**\n" +
            "   - Read-only access to all dashboards\n" +
            "   - Can view incidents, services, and resource metrics\n" +
            "   - Cannot create, modify, or resolve incidents\n\n" +
            "**Default Accounts:**\n" +
            "• `admin` — Admin role\n" +
            "• `engineer1` — Engineer role\n" +
            "• `viewer1` — Viewer role";
    }

    private String buildActionsResponse(String message) {
        if (message.toLowerCase().contains("assign")) {
            return "**Assigning an Incident**\n\n" +
                "To assign an incident to an engineer:\n" +
                "1. Open the incident from the Incidents list\n" +
                "2. Click **Edit** or the assignment field\n" +
                "3. Select the responsible engineer from the dropdown\n" +
                "4. Save the change\n\n" +
                "The assigned engineer will appear on the incident card and dashboard widget.\n" +
                "Best practice: Assign incidents immediately to avoid confusion about ownership.";
        }
        if (message.toLowerCase().contains("escalate")) {
            return "**Escalating an Incident**\n\n" +
                "To escalate the severity of an incident:\n" +
                "1. Open the incident detail view\n" +
                "2. Click **Edit Incident**\n" +
                "3. Change the **Severity** field (e.g., SEV3 → SEV1)\n" +
                "4. Update the description with escalation reason\n" +
                "5. Save — the dashboard will reflect the new severity immediately\n\n" +
                "⚠️ Escalating to SEV1 or SEV2 will trigger the critical alert banner on all team members' dashboards.";
        }
        return "**Incident Actions Guide**\n\n" +
            "Available actions on incidents:\n\n" +
            "• **Acknowledge** — Change status from OPEN to IN_PROGRESS\n" +
            "• **Assign** — Set the responsible engineer\n" +
            "• **Update** — Edit title, description, or severity\n" +
            "• **Escalate** — Increase severity level\n" +
            "• **Resolve** — Mark as resolved with resolution notes\n" +
            "• **Delete** — Admin only; use sparingly to preserve audit trail\n\n" +
            "Ask me specifically about: assigning, escalating, or resolving incidents.";
    }

    private String buildSeverityResponse() {
        return "**Incident Severity Levels**\n\n" +
            "Severity determines the urgency and response time required:\n\n" +
            "🔴 **SEV1 — Critical**\n" +
            "   - Complete service outage affecting all users\n" +
            "   - Production data at risk\n" +
            "   - Response time: Immediate (< 15 minutes)\n" +
            "   - Escalation: Entire on-call team + management\n\n" +
            "🟠 **SEV2 — High**\n" +
            "   - Major functionality impaired for many users\n" +
            "   - Significant business impact\n" +
            "   - Response time: < 1 hour\n" +
            "   - Escalation: On-call engineer + lead\n\n" +
            "🟡 **SEV3 — Medium**\n" +
            "   - Partial outage, workaround available\n" +
            "   - Limited user impact\n" +
            "   - Response time: < 4 hours\n" +
            "   - Escalation: On-call engineer\n\n" +
            "🔵 **SEV4 — Low**\n" +
            "   - Minor issue, cosmetic or edge case\n" +
            "   - Minimal user impact\n" +
            "   - Response time: Next business day\n" +
            "   - Escalation: Standard ticket queue";
    }

    private String buildHistoryResponse() {
        return "**Incident History and Reporting**\n\n" +
            "All incidents are permanently stored for audit and postmortem purposes.\n\n" +
            "**Accessing History:**\n" +
            "1. Navigate to **Incidents** in the sidebar\n" +
            "2. The list shows all incidents, including resolved ones\n" +
            "3. Use filters to narrow by status, severity, or date range\n\n" +
            "**What's Recorded:**\n" +
            "• Incident creation timestamp and creator\n" +
            "• All status transitions with timestamps\n" +
            "• Resolution notes and resolver\n" +
            "• Assigned engineer history\n\n" +
            "**Best Practices:**\n" +
            "• Always add detailed resolution notes — they become your postmortem baseline\n" +
            "• Use consistent titles so patterns are discoverable (e.g., 'Payment Service - DB Connection Failure')\n" +
            "• Correlate incidents with resource metric spikes for root cause analysis";
    }

    private String buildHelpResponse(String message) {
        if (message.toLowerCase().contains("chart") || message.toLowerCase().contains("graph")) {
            return "**Reading the Charts**\n\n" +
                "**Service Health Donut Chart:**\n" +
                "• Each segment represents a status category\n" +
                "• Green = UP, Yellow = DEGRADED, Red = DOWN, Grey = UNKNOWN\n" +
                "• Hover over segments to see exact counts\n\n" +
                "**Resource Progress Bars:**\n" +
                "• Blue bars = normal utilization (< 80%)\n" +
                "• Orange/red bars = above threshold (≥ 80%)\n" +
                "• The percentage shown is current utilization\n\n" +
                "All charts auto-refresh every 30 seconds alongside the rest of the dashboard data.";
        }
        return "**CloudOps Assistant — Help Guide**\n\n" +
            "I can explain any part of this operations dashboard. Try asking:\n\n" +
            "🔍 **Understanding Data**\n" +
            "• \"What do the severity levels mean?\"\n" +
            "• \"How do I read the service health chart?\"\n" +
            "• \"What triggers a resource alert?\"\n\n" +
            "⚡ **Taking Actions**\n" +
            "• \"How do I create an incident?\"\n" +
            "• \"How do I resolve an incident?\"\n" +
            "• \"How do I escalate severity?\"\n\n" +
            "🏗️ **Understanding the System**\n" +
            "• \"What are the different user roles?\"\n" +
            "• \"How does the dashboard work?\"\n" +
            "• \"Where can I find incident history?\"\n\n" +
            "Just type your question in plain English — I'll do my best to help!";
    }

    private String buildGeneralResponse() {
        return "**CloudOps Assistant**\n\n" +
            "I didn't quite catch that. I'm specialized in helping you understand this operations dashboard.\n\n" +
            "**I can help with:**\n" +
            "• Incident management and severity levels\n" +
            "• Service health monitoring\n" +
            "• Resource utilization and alerts\n" +
            "• User roles and permissions\n" +
            "• Dashboard navigation\n\n" +
            "Try asking something like:\n" +
            "• \"How do I create an incident?\"\n" +
            "• \"What does SEV1 mean?\"\n" +
            "• \"How do I resolve an incident?\"\n" +
            "• \"What are the different user roles?\"";
    }

    private List<String> generateSuggestions(String intent) {
        return switch (intent) {
            case "greeting" -> List.of(
                "How do I create an incident?",
                "What do the severity levels mean?",
                "Explain the dashboard sections"
            );
            case "incidents" -> List.of(
                "How do I resolve an incident?",
                "What does SEV1 mean?",
                "How do I assign an incident?"
            );
            case "services" -> List.of(
                "What does DEGRADED status mean?",
                "How often does health check run?",
                "How do I view service history?"
            );
            case "resources" -> List.of(
                "What triggers a resource alert?",
                "How do I view CPU trends?",
                "What is the alert threshold?"
            );
            case "roles" -> List.of(
                "What can an Engineer do?",
                "How do I get Admin access?",
                "What can a Viewer see?"
            );
            case "severity" -> List.of(
                "When should I use SEV1?",
                "How do I escalate an incident?",
                "What is the response time for SEV2?"
            );
            default -> List.of(
                "How does the dashboard work?",
                "What are the user roles?",
                "How do I create an incident?"
            );
        };
    }
}
