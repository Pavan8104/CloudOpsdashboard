import { Component, OnInit } from '@angular/core';
import { IncidentService } from '../../core/services/incident.service';
import { AuthService } from '../../core/auth/auth.service';
import { Incident, getSeverityClass, getStatusColor, isActiveIncident } from '../../shared/models/incident.model';

/**
 * Incidents Component - incident management page.
 *
 * Active aur all incidents ka table view hai yahan.
 * Resolve button ENGINEER/ADMIN ke liye dikhta hai.
 * Filter by severity, status, aur search by title.
 * Tab-based layout - Active vs All incidents alag dikhte hain.
 */
@Component({
  selector: 'app-incidents',
  templateUrl: './incidents.component.html',
  styleUrls: ['./incidents.component.scss']
})
export class IncidentsComponent implements OnInit {

  allIncidents: Incident[] = [];
  filteredIncidents: Incident[] = [];
  isLoading = true;
  selectedTab = 0;  // 0 = Active, 1 = All
  searchQuery = '';
  selectedSeverity = '';
  selectedStatus = '';

  displayedColumns = ['incidentNumber', 'severity', 'title', 'status', 'assigned', 'service', 'started', 'mttr', 'actions'];

  constructor(
    private incidentService: IncidentService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadIncidents();
  }

  loadIncidents(): void {
    this.isLoading = true;
    this.incidentService.getAll().subscribe({
      next: (incidents) => {
        this.allIncidents = incidents;
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  onTabChange(index: number): void {
    this.selectedTab = index;
    this.applyFilter();
  }

  applyFilter(): void {
    let result = [...this.allIncidents];

    // Tab filter - active tab pe sirf active incidents
    if (this.selectedTab === 0) {
      result = result.filter(i => isActiveIncident(i));
    }

    // Search filter
    if (this.searchQuery) {
      const q = this.searchQuery.toLowerCase();
      result = result.filter(i =>
        i.title.toLowerCase().includes(q) ||
        i.incidentNumber?.toLowerCase().includes(q)
      );
    }

    // Severity filter
    if (this.selectedSeverity) {
      result = result.filter(i => i.severity === this.selectedSeverity);
    }

    // Status filter
    if (this.selectedStatus) {
      result = result.filter(i => i.status === this.selectedStatus);
    }

    this.filteredIncidents = result;
  }

  resolveIncident(incident: Incident): void {
    const notes = prompt('Resolution notes do - kya kiya tha?');
    if (!notes) return;

    this.incidentService.resolve(incident.id, notes).subscribe({
      next: () => { this.loadIncidents(); },
      error: (err) => { console.error('Resolve failed:', err); }
    });
  }

  deleteIncident(id: number): void {
    if (confirm('Incident delete karna chahte ho? Yeh undo nahi ho sakta.')) {
      this.incidentService.delete(id).subscribe({
        next: () => { this.loadIncidents(); }
      });
    }
  }

  // Helper functions for template
  getSeverityClass = getSeverityClass;
  getStatusColor = getStatusColor;
  isActiveIncident = isActiveIncident;

  // Counts for tabs
  get activeCount(): number { return this.allIncidents.filter(i => isActiveIncident(i)).length; }
  get totalCount(): number { return this.allIncidents.length; }
}
