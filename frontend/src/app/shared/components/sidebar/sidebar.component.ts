import { Component, Input, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../../core/auth/auth.service';

import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

/**
 * Sidebar Component - left navigation menu.
 *
 * Role based navigation - VIEWER ko admin items nahi dikhenge.
 * Current route highlight hota hai automatically.
 * Collapsible hai - mobile pe aur compact mode ke liye.
 */
@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule,
    MatTooltipModule,
    MatDividerModule
  ]
})
export class SidebarComponent implements OnInit {

  // Parent se control hota hai - open/closed state
  @Input() isOpen = true;
  @Input() isMobile = false;

  currentRoute = '';

  // Navigation items - role ke basis pe filter honge
  navItems = [
    {
      label: 'Dashboard',
      icon: 'dashboard',
      route: '/dashboard',
      roles: [],  // Sab dekh sakte hain
    },
    {
      label: 'Service Health',
      icon: 'monitor_heart',
      route: '/services',
      roles: [],
    },
    {
      label: 'Incidents',
      icon: 'warning_amber',
      route: '/incidents',
      roles: [],
    },
    {
      label: 'Resource Usage',
      icon: 'bar_chart',
      route: '/resources',
      roles: [],
    },
    // Admin only items - divider ke baad
    {
      label: 'User Management',
      icon: 'group',
      route: '/admin/users',
      roles: ['ROLE_ADMIN'],  // Sirf admin dekh sakta hai
      adminOnly: true
    },
    {
      label: 'Settings',
      icon: 'settings',
      route: '/admin/settings',
      roles: ['ROLE_ADMIN'],
      adminOnly: true
    }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Current route track karo - active item highlight ke liye
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.currentRoute = event.urlAfterRedirects;
    });

    // Initial route set karo
    this.currentRoute = this.router.url;
  }

  /**
   * Nav item visible hai ki nahi - role check karo.
   */
  isVisible(item: any): boolean {
    if (!item.roles || item.roles.length === 0) return true;
    return item.roles.some((role: string) => this.authService.hasRole(role as any));
  }

  /**
   * Current route se match karta hai ya nahi - active class ke liye.
   */
  isActive(route: string): boolean {
    return this.currentRoute.startsWith(route);
  }

  get isAdmin(): boolean {
    return this.authService.isAdmin;
  }
}
