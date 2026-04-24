import { ServiceHealth, ServiceHealthSummary } from './service-health.model';
import { Incident } from './incident.model';
import { ResourceUsage } from './resource-usage.model';

export interface DashboardSummary {
  statusSummary: ServiceHealthSummary;
  allServices: ServiceHealth[];
  criticalIncidents: Incident[];
  activeIncidents: Incident[];
  alertingResources: ResourceUsage[];
  latestMetrics: ResourceUsage[];
  totalServices: number;
  activeIncidentCount: number;
  alertCount: number;
}
