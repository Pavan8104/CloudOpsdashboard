/**
 * Incident TypeScript model - backend Incident entity ke saath sync mein rehna chahiye.
 * SEV1-SEV4 aur status types TypeScript union types hain - compile time safety.
 */

export type Severity = 'SEV1' | 'SEV2' | 'SEV3' | 'SEV4';
export type IncidentStatus = 'OPEN' | 'IN_PROGRESS' | 'MONITORING' | 'RESOLVED' | 'CLOSED';

export interface Incident {
  id: number;
  incidentNumber: string;
  title: string;
  description: string;
  severity: Severity;
  status: IncidentStatus;
  affectedServiceId?: number;
  affectedServiceName?: string;
  assignedToId?: number;
  assignedToUsername?: string;
  assignedToFullName?: string;
  createdById?: number;
  createdByUsername?: string;
  startedAt: string;
  resolvedAt?: string;
  resolutionNotes?: string;
  affectedUsersCount?: number;
  mttr?: string;              // Backend calculate karta hai - "2h 30m" format
  severityColor?: string;     // Backend se - "red", "orange" etc.
  createdAt: string;
  updatedAt: string;
}

// Severity badge color ke liye CSS class - template mein use hoga
export function getSeverityClass(severity: Severity): string {
  const classMap: Record<Severity, string> = {
    SEV1: 'sev1',
    SEV2: 'sev2',
    SEV3: 'sev3',
    SEV4: 'sev4'
  };
  return classMap[severity] ?? 'sev4';
}

// Status ke liye Material chip color
export function getStatusColor(status: IncidentStatus): string {
  const colorMap: Record<IncidentStatus, string> = {
    OPEN: 'warn',
    IN_PROGRESS: 'primary',
    MONITORING: 'accent',
    RESOLVED: '',
    CLOSED: ''
  };
  return colorMap[status] ?? '';
}

// Active incident hai ya nahi - filter ke liye
export function isActiveIncident(incident: Incident): boolean {
  return ['OPEN', 'IN_PROGRESS', 'MONITORING'].includes(incident.status);
}
