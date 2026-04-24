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

/**
 * Advanced NLP-simulated Chatbot Service.
 * Handles a vast array of intents using weighted keyword and phrase matching
 * to provide a comprehensive CloudOps conversational experience.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final IncidentRepository incidentRepository;
    private final ServiceHealthRepository serviceHealthRepository;
    private final ResourceUsageRepository resourceUsageRepository;

    private static final Map<String, List<Pattern>> INTENT_DICT = new LinkedHashMap<>();

    static {
        // Incidents
        INTENT_DICT.put("incidents_create", compile("(?i)\\b(create|new|open|start|report)\\b.*\\b(incident|issue|outage|bug|problem)\\b"));
        INTENT_DICT.put("incidents_resolve", compile("(?i)\\b(resolve|close|fix|finish|done)\\b.*\\b(incident|issue|outage|bug|problem)\\b"));
        INTENT_DICT.put("incidents_assign", compile("(?i)\\b(assign|delegate|give)\\b.*\\b(incident|issue|outage|bug|problem)\\b"));
        INTENT_DICT.put("incidents_escalate", compile("(?i)\\b(escalate|upgrade|increase)\\b.*\\b(severity|incident|issue)\\b"));
        INTENT_DICT.put("incidents_general", compile("(?i)\\b(incident|incidents|outage|outages|down|alert|critical)\\b"));
        
        // Services
        INTENT_DICT.put("services_deploy", compile("(?i)\\b(deploy|release|ship|push|update)\\b.*\\b(service|app|application|code)\\b"));
        INTENT_DICT.put("services_logs", compile("(?i)\\b(log|logs|trace|debug|error)\\b.*\\b(service|app)\\b"));
        INTENT_DICT.put("services_health", compile("(?i)\\b(health|status|uptime|monitor|monitoring)\\b.*\\b(service|services|app)\\b"));
        INTENT_DICT.put("services_general", compile("(?i)\\b(service|services|app|application|system)\\b"));

        // Resources & Infrastructure
        INTENT_DICT.put("resources_cpu", compile("(?i)\\b(cpu|processor|compute|processing)\\b"));
        INTENT_DICT.put("resources_memory", compile("(?i)\\b(ram|memory|heap|leak)\\b"));
        INTENT_DICT.put("resources_disk", compile("(?i)\\b(disk|storage|space|volume|drive)\\b"));
        INTENT_DICT.put("resources_scale", compile("(?i)\\b(scale|scaling|auto-scale|resize|bigger|smaller)\\b"));
        INTENT_DICT.put("resources_general", compile("(?i)\\b(resource|resources|metric|metrics|utilization|capacity)\\b"));

        // Severity
        INTENT_DICT.put("severity_levels", compile("(?i)\\b(sev|sev1|sev2|sev3|sev4|severity|priority|p1|p2|p3|p4)\\b"));

        // Roles & Security
        INTENT_DICT.put("roles_admin", compile("(?i)\\b(admin|administrator|root)\\b"));
        INTENT_DICT.put("roles_engineer", compile("(?i)\\b(engineer|dev|developer|sre|ops)\\b"));
        INTENT_DICT.put("roles_viewer", compile("(?i)\\b(viewer|read-only|guest)\\b"));
        INTENT_DICT.put("roles_general", compile("(?i)\\b(role|roles|permission|permissions|access|login|auth)\\b"));

        // Cost & Optimization
        INTENT_DICT.put("best_practices_cost", compile("(?i)\\b(cost|billing|expensive|save money|budget|reduce)\\b"));
        INTENT_DICT.put("best_practices_reliability", compile("(?i)\\b(reliable|reliability|best practice|resilient|resiliency)\\b"));

        // Dashboard & UI
        INTENT_DICT.put("dashboard_navigation", compile("(?i)\\b(navigate|find|where|locate|how to use)\\b"));
        INTENT_DICT.put("dashboard_general", compile("(?i)\\b(dashboard|overview|summary|home|main|ui|interface)\\b"));

        // Support & History
        INTENT_DICT.put("history", compile("(?i)\\b(report|export|history|past|previous|audit)\\b"));
        INTENT_DICT.put("support", compile("(?i)\\b(support|contact|help desk|manager|boss)\\b"));

        // Conversational / Fun
        INTENT_DICT.put("greeting", compile("(?i)\\b(hello|hi|hey|start|begin|greet|morning|evening|afternoon)\\b"));
        INTENT_DICT.put("identity", compile("(?i)\\b(who are you|what are you|are you ai|bot|robot)\\b"));
        INTENT_DICT.put("capabilities", compile("(?i)\\b(what can you do|help me|how can you help|features)\\b"));
        INTENT_DICT.put("thanks", compile("(?i)\\b(thanks|thank you|appreciate|good job)\\b"));
    }

    private static List<Pattern> compile(String regex) {
        return List.of(Pattern.compile(regex));
    }

    public ChatResponse process(ChatRequest request) {
        String message = request.getMessage().trim();
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();
        
        String intent = detectIntent(message);
        log.debug("NLP Chatbot identified intent: '{}' for message: '{}'", intent, message);

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
        // Fallback fuzzy matching: Check for multi-word exact hits first
        for (Map.Entry<String, List<Pattern>> entry : INTENT_DICT.entrySet()) {
            for (Pattern pattern : entry.getValue()) {
                if (pattern.matcher(message).find()) {
                    return entry.getKey();
                }
            }
        }
        return "general_help";
    }

    private String generateResponse(String intent, String message) {
        return switch (intent) {
            // Incidents
            case "incidents_create" -> "**Creating an Incident**\n\n1. Go to **Incidents** on the left menu.\n2. Click **+ New Incident**.\n3. Provide Title, Severity, Service, and Description.\n4. Save. (Requires Engineer/Admin role).";
            case "incidents_resolve" -> "**Resolving an Incident**\n\n1. Open the incident.\n2. Click **Resolve**.\n3. You MUST provide detailed **Resolution Notes** (this is for the postmortem).\n4. The MTTR will be calculated automatically.";
            case "incidents_assign" -> "**Assigning an Incident**\n\nEdit the incident and select an engineer from the 'Assigned To' dropdown. The assignee will see it on their dashboard immediately.";
            case "incidents_escalate" -> "**Escalating an Incident**\n\nEdit the incident and bump the Severity up (e.g., SEV3 to SEV1). Note: SEV1 and SEV2 trigger a massive red banner across the entire team's dashboard.";
            case "incidents_general" -> String.format("**Incident Overview**\n\nWe currently track %d incidents. An incident flows from OPEN → IN_PROGRESS → RESOLVED. Ask me how to create, resolve, or escalate one.", incidentRepository.count());

            // Services
            case "services_deploy" -> "**Deployments**\n\nThis dashboard monitors health, but deployments are handled via your CI/CD pipeline (e.g., GitHub Actions or GitLab CI). Once deployed, monitor the 'Service Health' tab here to ensure it comes up 🟢 UP.";
            case "services_logs" -> "**Viewing Logs**\n\nFor deep application logs, check your centralized logging tool (e.g., Datadog, ELK, GCP Cloud Logging). This dashboard focuses on top-level Health Status and Resource Metrics.";
            case "services_health" -> "**Service Health**\n\nStatus is determined by active probes. \n🟢 UP\n🟡 DEGRADED (slow response)\n🔴 DOWN (failing probes)\n⚪ UNKNOWN. Navigate to the Services tab for deep latency metrics.";
            case "services_general" -> String.format("**Services Overview**\n\nMonitoring %d services currently. The dashboard donut chart gives you an instant visual of your fleet's health.", serviceHealthRepository.count());

            // Resources
            case "resources_cpu" -> "**CPU Metrics**\n\nCPU utilization over 80% triggers an alert. If a service consistently hits high CPU, consider horizontally scaling (adding more instances) or profiling the code.";
            case "resources_memory" -> "**Memory (RAM) Metrics**\n\nHigh memory usage might indicate a memory leak. If it hits the red zone (>80%), the JVM might OOM crash soon. Check the 'Resource Utilization' panel.";
            case "resources_disk" -> "**Disk Storage Metrics**\n\nDisk running out is a silent killer. Ensure log rotation is active and database volumes have auto-grow enabled. Check the metrics tab for alerts.";
            case "resources_scale" -> "**Scaling Services**\n\nIf resources are in the red, you need to scale. For GKE/K8s, adjust your HPA (Horizontal Pod Autoscaler). For Cloud Run, increase max-instances.";
            case "resources_general" -> String.format("**Resource Overview**\n\nWe ingest thousands of data points. Currently holding %d metric records. Anything over 80%% utilization triggers a dashboard alert.", resourceUsageRepository.count());

            // Severity
            case "severity_levels" -> "**Severity Matrix**\n\n🔴 **SEV1**: Critical system down. Drop everything.\n🟠 **SEV2**: Major feature broken. High priority.\n🟡 **SEV3**: Partial degradation. Workaround exists.\n🔵 **SEV4**: Minor/Cosmetic. Handle in regular sprint.";

            // Roles
            case "roles_admin" -> "**Admin Role**\n\nAdmins have 'God Mode'. They can create, edit, resolve, and DELETE incidents. They also manage users.";
            case "roles_engineer" -> "**Engineer Role**\n\nEngineers are the operators. They can create, edit, and resolve incidents, and view all system metrics. They cannot delete history.";
            case "roles_viewer" -> "**Viewer Role**\n\nViewers (like product managers) can look at dashboards and incidents but cannot alter system state or touch incidents.";
            case "roles_general" -> "**Access Control**\n\nThe dashboard uses RBAC: ADMIN, ENGINEER, and VIEWER. You are authenticated via a secure JWT token.";

            // Best Practices
            case "best_practices_cost" -> "**Cost Optimization**\n\nTo save money:\n1. Scale down dev/staging environments at night.\n2. Delete unattached persistent volumes.\n3. Ensure DB instances are right-sized (check CPU/RAM metrics here).";
            case "best_practices_reliability" -> "**Reliability Practices**\n\n1. Always write detailed incident resolution notes.\n2. Set up automated scaling.\n3. Keep an eye on the 'Resource Alerts' section of this dashboard.";

            // Dashboard
            case "dashboard_navigation" -> "**Navigation Help**\n\nUse the left sidebar. **Dashboard** is the summary. **Services** shows latency and status. **Incidents** is your ticketing system. **Resources** shows charts.";
            case "dashboard_general" -> "**The Dashboard**\n\nThe main view aggregates data from Services, Incidents, and Resources into one 'Super View' that auto-refreshes every 30 seconds.";

            // Support & History
            case "history" -> "**Audit & History**\n\nWe never delete data unless an Admin forces it. You can view all past incidents in the 'Incidents' tab by filtering by the 'RESOLVED' status.";
            case "support" -> "**Need Human Help?**\n\nIf the system is completely broken, ping the primary On-Call engineer via PagerDuty or your internal Slack #ops-critical channel.";

            // Conversational
            case "greeting" -> "👋 **Hello there!** I am your CloudOps AI. I can analyze system health, guide you through incident resolution, or explain infrastructure metrics. What's on your mind?";
            case "identity" -> "🤖 **I am the CloudOps Assistant.** An AI designed to help Site Reliability Engineers (SREs) and Developers navigate this observability platform.";
            case "capabilities" -> "**My Capabilities**\n\nI am wired into the dashboard's brain. Ask me about:\n- Incident creation/resolution\n- Service Health\n- CPU/Memory metrics\n- Severity levels\n- Or use your Voice (click the mic!)";
            case "thanks" -> "You're very welcome! Stay frosty and keep those systems 🟢 UP.";

            default -> "**CloudOps Assistant**\n\nI'm not entirely sure how to answer that. Try asking me about:\n• \"How to create an incident\"\n• \"What are the severity levels?\"\n• \"How to read CPU metrics\"\n• \"What does an Engineer role do?\"";
        };
    }

    private List<String> generateSuggestions(String intent) {
        if (intent.startsWith("incidents")) {
            return List.of("What is SEV1?", "How do I resolve an incident?", "Assigning incidents");
        } else if (intent.startsWith("services")) {
            return List.of("How do I view logs?", "What does DEGRADED mean?", "Deploying services");
        } else if (intent.startsWith("resources")) {
            return List.of("Scaling services", "CPU alerts", "Cost optimization");
        } else if (intent.startsWith("roles")) {
            return List.of("Admin permissions", "Viewer access", "Engineer responsibilities");
        } else if (intent.equals("greeting") || intent.equals("identity")) {
            return List.of("What can you do?", "How to use the dashboard", "Create an incident");
        }
        return List.of("Service Health", "Resource Metrics", "Incident Management");
    }
}
