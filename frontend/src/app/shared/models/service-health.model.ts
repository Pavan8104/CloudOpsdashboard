/**
 * ServiceHealth TypeScript model - backend ke ServiceHealth entity se match karta hai.
 * Yeh types Angular components mein type safety ke liye use hote hain.
 */

export type HealthStatus = 'UP' | 'DOWN' | 'DEGRADED' | 'UNKNOWN' | 'MAINTENANCE';

export interface ServiceHealth {
  id: number;
  serviceName: string;
  serviceType: string;
  status: HealthStatus;
  region: string;
  gcpProjectId: string;
  responseTimeMs: number;
  uptimePercentage: number;
  lastCheckedAt: string;    // ISO date string - backend se aise aata hai
  statusMessage: string;
  statusColor: string;      // Backend calculate karta hai - green/red/yellow
  createdAt: string;
  updatedAt: string;
}

// Dashboard summary widget ke liye - kitni UP, DOWN etc.
export interface ServiceHealthSummary {
  UP?: number;
  DOWN?: number;
  DEGRADED?: number;
  MAINTENANCE?: number;
  UNKNOWN?: number;
}

// Status ke basis pe badge CSS class return karo - template mein use hoga
export function getStatusClass(status: HealthStatus): string {
  const classMap: Record<HealthStatus, string> = {
    UP: 'status-up',
    DOWN: 'status-down',
    DEGRADED: 'status-degraded',
    MAINTENANCE: 'status-maintenance',
    UNKNOWN: 'status-unknown'
  };
  return classMap[status] ?? 'status-unknown';
}

// Material icon name for status - visual indicator ke liye
export function getStatusIcon(status: HealthStatus): string {
  const iconMap: Record<HealthStatus, string> = {
    UP: 'check_circle',
    DOWN: 'cancel',
    DEGRADED: 'warning',
    MAINTENANCE: 'build',
    UNKNOWN: 'help'
  };
  return iconMap[status] ?? 'help';
}
