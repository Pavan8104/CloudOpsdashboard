import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';
import { CurrentUser } from '../../../core/auth/auth.model';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

/**
 * Navbar Component - top navigation bar.
 *
 * User info dikhaata hai, logout button hai, aur mobile pe menu toggle.
 * Sidebar toggle event emit karta hai - parent layout component handle karta hai.
 * Role based user avatar/badge bhi yahan hoga.
 */
@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatTooltipModule,
    MatDividerModule
  ]
})
export class NavbarComponent implements OnInit {

  // Sidebar open/close toggle parent ko batata hai
  @Output() sidebarToggle = new EventEmitter<void>();

  currentUser: CurrentUser | null = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Reactive subscription - user change hone pe automatically update
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  /**
   * User initials banao display ke liye - "Rahul Kumar" -> "RK"
   */
  getUserInitials(): string {
    if (!this.currentUser?.fullName) {
      return this.currentUser?.username?.charAt(0).toUpperCase() ?? 'U';
    }

    const parts = this.currentUser.fullName.split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return parts[0][0].toUpperCase();
  }

  /**
   * Primary role return karo - badge ke liye (ADMIN, ENGINEER, VIEWER)
   */
  getPrimaryRole(): string {
    const roles = this.currentUser?.roles ?? [];
    if (roles.includes('ROLE_ADMIN')) return 'Admin';
    if (roles.includes('ROLE_ENGINEER')) return 'Engineer';
    return 'Viewer';
  }

  /**
   * Logout karo - AuthService handle karta hai storage clear aur redirect.
   */
  logout(): void {
    this.authService.logout();
  }

  onSidebarToggle(): void {
    this.sidebarToggle.emit();
  }
}
