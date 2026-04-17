import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ResourceUsageService } from '../../core/services/resource-usage.service';
import { ResourceUsage, ResourceType, getResourceTypeLabel, getUtilizationColor, toChartData } from '../../shared/models/resource-usage.model';
import { ChartConfiguration, ChartType } from 'chart.js';

/**
 * Resource Usage Component - CPU, Memory, Network charts.
 *
 * ng2-charts use kar rahe hain - Chart.js ke upar angular wrapper.
 * Service select karo toh uske charts dikhe.
 * Time range selector hai - last 1h, 6h, 24h, 7d.
 * Auto-refresh kar rahe hain real-time feel ke liye.
 */
@Component({
  selector: 'app-resource-usage',
  templateUrl: './resource-usage.component.html',
  styleUrls: ['./resource-usage.component.scss']
})
export class ResourceUsageComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  isLoading = true;
  latestMetrics: ResourceUsage[] = [];
  alertingResources: ResourceUsage[] = [];

  // Unique services list - select karne ke liye
  serviceNames: string[] = [];
  selectedService = '';

  // Time range options
  timeRanges = [
    { label: 'Last 1h', hours: 1 },
    { label: 'Last 6h', hours: 6 },
    { label: 'Last 24h', hours: 24 },
    { label: 'Last 7d', hours: 168 }
  ];
  selectedTimeRange = 1;

  // CPU Chart config
  cpuChartData: ChartConfiguration['data'] = {
    labels: [],
    datasets: [{
      label: 'CPU Usage (%)',
      data: [],
      borderColor: '#1a73e8',
      backgroundColor: 'rgba(26, 115, 232, 0.1)',
      fill: true,
      tension: 0.4  // Smooth curve
    }]
  };

  memoryChartData: ChartConfiguration['data'] = {
    labels: [],
    datasets: [{
      label: 'Memory Usage',
      data: [],
      borderColor: '#34a853',
      backgroundColor: 'rgba(52, 168, 83, 0.1)',
      fill: true,
      tension: 0.4
    }]
  };

  lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      y: {
        beginAtZero: true,
        grid: { color: '#f1f3f4' }
      },
      x: {
        grid: { display: false }
      }
    },
    plugins: {
      legend: { display: true },
      tooltip: { mode: 'index', intersect: false }
    }
  };

  lineChartType: ChartType = 'line';

  constructor(private resourceUsageService: ResourceUsageService) {}

  ngOnInit(): void {
    this.loadLatestMetrics();
    this.loadAlerts();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadLatestMetrics(): void {
    this.isLoading = true;
    this.resourceUsageService.getLatestAll()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (metrics) => {
          this.latestMetrics = metrics;
          // Unique service names extract karo
          this.serviceNames = [...new Set(metrics.map(m => m.serviceName))];
          if (this.serviceNames.length > 0 && !this.selectedService) {
            this.selectedService = this.serviceNames[0];
            this.loadServiceHistory();
          }
          this.isLoading = false;
        },
        error: () => { this.isLoading = false; }
      });
  }

  loadAlerts(): void {
    this.resourceUsageService.getAlerts()
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: (alerts) => { this.alertingResources = alerts; } });
  }

  loadServiceHistory(): void {
    if (!this.selectedService) return;

    const end = new Date();
    const start = new Date();
    start.setHours(start.getHours() - this.selectedTimeRange);

    // CPU history
    this.resourceUsageService.getHistory(this.selectedService, start, end, 'CPU')
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (metrics) => {
          const chartPoints = toChartData(metrics);
          this.cpuChartData = {
            labels: chartPoints.map(p => p.x),
            datasets: [{
              label: 'CPU Usage (%)',
              data: chartPoints.map(p => p.y),
              borderColor: '#1a73e8',
              backgroundColor: 'rgba(26, 115, 232, 0.1)',
              fill: true,
              tension: 0.4
            }]
          };
        }
      });

    // Memory history
    this.resourceUsageService.getHistory(this.selectedService, start, end, 'MEMORY')
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (metrics) => {
          const chartPoints = toChartData(metrics);
          this.memoryChartData = {
            labels: chartPoints.map(p => p.x),
            datasets: [{
              label: 'Memory Usage (MB)',
              data: chartPoints.map(p => p.y),
              borderColor: '#34a853',
              backgroundColor: 'rgba(52, 168, 83, 0.1)',
              fill: true,
              tension: 0.4
            }]
          };
        }
      });
  }

  onServiceChange(service: string): void {
    this.selectedService = service;
    this.loadServiceHistory();
  }

  onTimeRangeChange(hours: number): void {
    this.selectedTimeRange = hours;
    this.loadServiceHistory();
  }

  // Helper functions
  getResourceTypeLabel = getResourceTypeLabel;
  getUtilizationColor = getUtilizationColor;

  // Current service metrics - gauges ke liye
  getLatestForType(type: ResourceType): ResourceUsage | undefined {
    return this.latestMetrics.find(m =>
      m.serviceName === this.selectedService && m.resourceType === type
    );
  }

  get cpuUsage(): number { return this.getLatestForType('CPU')?.utilizationPercentage ?? 0; }
  get memoryUsage(): number { return this.getLatestForType('MEMORY')?.utilizationPercentage ?? 0; }
}
