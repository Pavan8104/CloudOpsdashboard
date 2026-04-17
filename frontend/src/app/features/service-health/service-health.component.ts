import { Component, OnInit } from '@angular/core';
import { ServiceHealthService } from '../../core/services/service-health.service';
import { AuthService } from '../../core/auth/auth.service';
import { ServiceHealth, getStatusClass, getStatusIcon } from '../../shared/models/service-health.model';

import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

/**
 * Service Health Component - sabhi GCP services ka status table.
 *
 * Filter, search, aur status update yahan se hota hai.
 * Admin/Engineer status manually update kar sakte hain yahan se.
 * Real-time feel ke liye lastCheckedAt prominently show karo.
 */
@Component({
  selector: 'app-service-health',
  templateUrl: './service-health.component.html',
  styleUrls: ['./service-health.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatTableModule,
    MatChipsModule,
    MatIconModule,
    MatButtonModule,
    MatMenuModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule
  ]
})
export class ServiceHealthComponent implements OnInit {

  services: ServiceHealth[] = [];
  filteredServices: ServiceHealth[] = [];
  isLoading = true;
  searchQuery = '';
  selectedStatus = '';
  selectedRegion = '';

  // Table columns
  displayedColumns = ['serviceName', 'serviceType', 'status', 'region', 'responseTime', 'uptime', 'lastChecked', 'actions'];

  // Unique regions for filter dropdown
  regions: string[] = [];

  constructor(
    private serviceHealthService: ServiceHealthService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadServices();
  }

  loadServices(): void {
    this.isLoading = true;
    this.serviceHealthService.getAll().subscribe({
      next: (services) => {
        this.services = services;
        this.filteredServices = services;
        // Unique regions nikalo filter ke liye
        this.regions = [...new Set(services.map(s => s.region).filter(Boolean))];
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  // Search aur filter apply karo
  applyFilter(): void {
    this.filteredServices = this.services.filter(service => {
      const matchesSearch = !this.searchQuery ||
        service.serviceName.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
        service.serviceType?.toLowerCase().includes(this.searchQuery.toLowerCase());

      const matchesStatus = !this.selectedStatus || service.status === this.selectedStatus;
      const matchesRegion = !this.selectedRegion || service.region === this.selectedRegion;

      return matchesSearch && matchesStatus && matchesRegion;
    });
  }

  onSearchChange(query: string): void {
    this.searchQuery = query;
    this.applyFilter();
  }

  onStatusFilter(status: string): void {
    this.selectedStatus = status;
    this.applyFilter();
  }

  onRegionFilter(region: string): void {
    this.selectedRegion = region;
    this.applyFilter();
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.selectedStatus = '';
    this.selectedRegion = '';
    this.filteredServices = this.services;
  }

  // Helper functions for template
  getStatusClass = getStatusClass;
  getStatusIcon = getStatusIcon;

  deleteService(id: number): void {
    if (confirm('Is service ko monitoring se remove karna chahte ho?')) {
      this.serviceHealthService.delete(id).subscribe({
        next: () => { this.loadServices(); },
        error: (err) => { console.error('Delete failed:', err); }
      });
    }
  }

  // Summary stats
  get totalCount(): number { return this.services.length; }
  get upCount(): number { return this.services.filter(s => s.status === 'UP').length; }
  get downCount(): number { return this.services.filter(s => s.status === 'DOWN').length; }
  get degradedCount(): number { return this.services.filter(s => s.status === 'DEGRADED').length; }
}
