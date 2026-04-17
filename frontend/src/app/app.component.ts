import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from './core/auth/auth.service';

/**
 * Root App Component - application ka shell component.
 *
 * Navbar aur Sidebar yahan hain - layout manage karta hai.
 * Login page pe sidebar/navbar nahi dikhna chahiye - route check karta hai.
 * Sidebar toggle state yahan manage hoti hai - responsive ke liye.
 */
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  // Sidebar open/closed state - navbar se toggle hota hai
  sidebarOpen = true;

  // Current route track karo - login page pe layout nahi dikhana
  currentRoute = '';

  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      this.currentRoute = event.urlAfterRedirects;
      // Mobile pe sidebar auto-close on navigation
      if (window.innerWidth < 900) {
        this.sidebarOpen = false;
      }
    });

    // Initial route set karo
    this.currentRoute = this.router.url;

    // Mobile devices pe default closed rakho sidebar
    if (window.innerWidth < 900) {
      this.sidebarOpen = false;
    }
  }

  /**
   * Navbar ka sidebar toggle button yeh call karta hai.
   */
  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  /**
   * Login page pe hai toh layout nahi dikhaana.
   * Logout ke baad bhi login page pe jaata hai.
   */
  get showLayout(): boolean {
    return this.authService.isLoggedIn &&
           !this.currentRoute.includes('/login');
  }
}
