import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, interval } from 'rxjs';
import { takeUntil, switchMap, startWith } from 'rxjs/operators';
import { ServiceHealthService } from '../../core/services/service-health.service';
import { IncidentService } from '../../core/services/incident.service';
import { ResourceUsageService } from '../../core/services/resource-usage.service';
import { AuthService } from '../../core/auth/auth.service';
import { ServiceHealth, ServiceHealthSummary } from '../../shared/models/service-health.model';
import { Incident } from '../../shared/models/incident.model';
import { ResourceUsage } from '../../shared/models/resource-usage.model';
import { ChartConfiguration, ChartType } from 'chart.js';

/**
 * Dashboard Component - CloudOps ka main landing page.
 *
 * Yahan sab kuch ek jagah dikhai deta hai - service health, active incidents, resource usage.
 * Auto-refresh kar rahe hain har 30 seconds pe - live ops ke liye important hai.
 * Charts ke liye ng2-charts use kar rahe hain - Chart.js ke upar wrapper hai.
 * OnDestroy mein unsubscribe karna mat bhulna - memory leak hoga warna.
 */
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {

  // Component destroy hone pe subscriptions clean up karne ke liye
  private destroy$ = new Subject<void>();

  // Data loading states
  isLoadingServices = true;
  isLoadingIncidents = true;
  isLoadingMetrics = true;

  // Data
  services: ServiceHealth[] = [];
  statusSummary: ServiceHealthSummary = {};
  activeIncidents: Incident[] = [];
  criticalIncidents: Incident[] = [];
  latestMetrics: ResourceUsage[] = [];
  alertingResources: ResourceUsage[] = [];

  // Last refresh time - user ko pata chale data fresh hai
  lastRefreshed = new Date();

  // Donut chart ke liye - service health overview
  serviceHealthChartData: ChartConfiguration['data'] = {
    labels: ['UP', 'Degraded', 'Down', 'Unknown'],
    datasets: [{
      data: [0, 0, 0, 0],
      backgroundColor: ['#34a853', '#fbbc04', '#ea4335', '#9aa0a6'],
      borderWidth: 0
    }]
  };

  serviceHealthChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { position: 'right' }
    }
  };

  serviceHealthChartType: ChartType = 'doughnut';

  constructor(
    private serviceHealthService: ServiceHealthService,
    private incidentService: IncidentService,
    private resourceUsageService: ResourceUsageService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    // Initial load aur phir har 30 seconds pe refresh
    interval(30000).pipe(
      startWith(0),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.loadDashboardData();
    });
  }

  ngOnDestroy(): void {
    // Sab subscriptions cancel karo - memory leak prevent karo
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadDashboardData(): void {
    this.loadServices();
    this.loadIncidents();
    this.loadMetrics();
    this.lastRefreshed = new Date();
  }

  private loadServices(): void {
    this.isLoadingServices = true;

    this.serviceHealthService.getSummary()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (summary) => {
          this.statusSummary = summary;
          // Chart data update karo
          this.serviceHealthChartData = {
            ...this.serviceHealthChartData,
            datasets: [{
              data: [
                summary.UP ?? 0,
                summary.DEGRADED ?? 0,
                summary.DOWN ?? 0,
                summary.UNKNOWN ?? 0
              ],
              backgroundColor: ['#34a853', '#fbbc04', '#ea4335', '#9aa0a6'],
              borderWidth: 0
            }]
          };
        }
      });

    this.serviceHealthService.getAll()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (services) => {
          this.services = services;
          this.isLoadingServices = false;
        },
        error: () => { this.isLoadingServices = false; }
      });
  }

  private loadIncidents(): void {
    this.isLoadingIncidents = true;

    this.incidentService.getCritical()
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: (incidents) => { this.criticalIncidents = incidents; } });

    this.incidentService.getActive()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (incidents) => {
          this.activeIncidents = incidents;
          this.isLoadingIncidents = false;
        },
        error: () => { this.isLoadingIncidents = false; }
      });
  }

  private loadMetrics(): void {
    this.isLoadingMetrics = true;

    this.resourceUsageService.getAlerts()
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: (alerts) => { this.alertingResources = alerts; } });

    this.resourceUsageService.getLatestAll()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (metrics) => {
          this.latestMetrics = metrics;
          this.isLoadingMetrics = false;
        },
        error: () => { this.isLoadingMetrics = false; }
      });
  }

  // Summary counts - template mein use hote hain
  get totalServices(): number { return this.services.length; }
  get healthyServices(): number { return this.statusSummary.UP ?? 0; }
  get downServices(): number { return this.statusSummary.DOWN ?? 0; }
  get activeIncidentCount(): number { return this.activeIncidents.length; }
  get criticalIncidentCount(): number { return this.criticalIncidents.length; }
  get alertCount(): number { return this.alertingResources.length; }

  // Manual refresh button ke liye
  onRefresh(): void {
    this.loadDashboardData();
  }
}
