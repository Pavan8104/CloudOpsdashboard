/**
 * ResourceUsage TypeScript model - metrics data ke liye types.
 * Chart.js/ng2-charts ke data format mein convert karne ke liye helper bhi hai.
 */

export type ResourceType = 'CPU' | 'MEMORY' | 'DISK' | 'NETWORK_IN' | 'NETWORK_OUT' | 'REQUESTS' | 'ERROR_RATE' | 'LATENCY';

export interface ResourceUsage {
  id: number;
  serviceName: string;
  resourceType: ResourceType;
  value: number;
  unit: string;
  maxCapacity?: number;
  alertThreshold?: number;
  region?: string;
  zone?: string;
  gcpProjectId?: string;
  utilizationPercentage?: number;  // Backend calculate karta hai
  alertTriggered?: boolean;         // Backend se
  displayLabel?: string;
  recordedAt: string;
  createdAt: string;
}

// Chart data format - ng2-charts ke liye
export interface ChartDataPoint {
  x: string;   // timestamp label
  y: number;   // value
}

// Ek service ke ek resource type ka time series - chart mein ek line
export function toChartData(metrics: ResourceUsage[]): ChartDataPoint[] {
  return metrics.map(m => ({
    x: new Date(m.recordedAt).toLocaleTimeString(),
    y: m.value
  }));
}

// Resource type ke liye display naam - chart legend mein
export function getResourceTypeLabel(type: ResourceType): string {
  const labels: Record<ResourceType, string> = {
    CPU: 'CPU Usage (%)',
    MEMORY: 'Memory Usage',
    DISK: 'Disk Usage',
    NETWORK_IN: 'Network In (Mbps)',
    NETWORK_OUT: 'Network Out (Mbps)',
    REQUESTS: 'Requests/sec',
    ERROR_RATE: 'Error Rate (%)',
    LATENCY: 'Latency (ms)'
  };
  return labels[type] ?? type;
}

// Utilization level ke basis pe color - gauge chart ke liye
export function getUtilizationColor(percentage: number): string {
  if (percentage >= 90) return '#ea4335';   // Critical - red
  if (percentage >= 75) return '#fbbc04';   // Warning - yellow
  return '#34a853';                          // Normal - green
}
