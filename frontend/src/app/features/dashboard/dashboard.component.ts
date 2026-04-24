import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, interval } from 'rxjs';
import { takeUntil, startWith } from 'rxjs/operators';
import { DashboardService } from '../../core/services/dashboard.service';
import { AuthService } from '../../core/auth/auth.service';
import { ServiceHealth, ServiceHealthSummary } from '../../shared/models/service-health.model';
import { Incident } from '../../shared/models/incident.model';
import { ResourceUsage } from '../../shared/models/resource-usage.model';
import { ChartConfiguration, ChartType } from 'chart.js';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  isLoading = true;

  services: ServiceHealth[] = [];
  statusSummary: ServiceHealthSummary = {};
  activeIncidents: Incident[] = [];
  criticalIncidents: Incident[] = [];
  latestMetrics: ResourceUsage[] = [];
  alertingResources: ResourceUsage[] = [];
  
  // Counts
  totalServicesCount = 0;
  activeIncidentsCount = 0;
  alertsCount = 0;

  lastRefreshed = new Date();

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
    private dashboardService: DashboardService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    interval(30000).pipe(
      startWith(0),
      takeUntil(this.destroy$)
    ).subscribe(() => {
      this.loadDashboardData();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadDashboardData(): void {
    this.isLoading = true;
    this.dashboardService.getSummary()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (summary) => {
          this.statusSummary = summary.statusSummary;
          this.services = summary.allServices;
          this.criticalIncidents = summary.criticalIncidents;
          this.activeIncidents = summary.activeIncidents;
          this.alertingResources = summary.alertingResources;
          this.latestMetrics = summary.latestMetrics;
          this.totalServicesCount = summary.totalServices;
          this.activeIncidentsCount = summary.activeIncidentCount;
          this.alertsCount = summary.alertCount;

          this.updateChart(summary.statusSummary);
          
          this.isLoading = false;
          this.lastRefreshed = new Date();
        },
        error: (err) => {
          console.error('Failed to load dashboard data', err);
          this.isLoading = false;
        }
      });
  }

  private updateChart(summary: ServiceHealthSummary): void {
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

  // Summary counts
  get totalServices(): number { return this.totalServicesCount; }
  get healthyServices(): number { return this.statusSummary.UP ?? 0; }
  get downServices(): number { return this.statusSummary.DOWN ?? 0; }
  get activeIncidentCount(): number { return this.activeIncidentsCount; }
  get criticalIncidentCount(): number { return this.criticalIncidents.length; }
  get alertCount(): number { return this.alertsCount; }

  onRefresh(): void {
    this.loadDashboardData();
  }
}

