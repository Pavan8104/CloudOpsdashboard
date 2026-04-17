import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';

// Lazy loading use kar rahe hain feature modules ke liye - initial bundle size kam hoga
// Abhi ke liye direct imports kar rahe hain - modules alag nahi kiye hain simplicity ke liye
import { LoginComponent } from './features/login/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { ServiceHealthComponent } from './features/service-health/service-health.component';
import { IncidentsComponent } from './features/incidents/incidents.component';
import { ResourceUsageComponent } from './features/resource-usage/resource-usage.component';

/**
 * Application routing - URL se component mapping yahan hoti hai.
 *
 * Protected routes pe AuthGuard lagaya hai - login required hai.
 * Admin routes pe RoleGuard bhi hai - sirf ADMIN access kar sakta hai.
 * Default route / ko /login pe redirect karo.
 */
const routes: Routes = [
  // Default redirect
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },

  // Login - public route, koi guard nahi
  {
    path: 'login',
    component: LoginComponent
  },

  // Dashboard - all authenticated users
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard]
  },

  // Service Health - all authenticated users
  {
    path: 'services',
    component: ServiceHealthComponent,
    canActivate: [AuthGuard]
  },

  // Incidents - all authenticated users
  {
    path: 'incidents',
    component: IncidentsComponent,
    canActivate: [AuthGuard]
  },

  // Resource Usage - all authenticated users
  {
    path: 'resources',
    component: ResourceUsageComponent,
    canActivate: [AuthGuard]
  },

  // 404 - catch all
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    // Scroll position reset on navigation - better UX
    scrollPositionRestoration: 'top',
    // Anchor scrolling - for future hash navigation
    anchorScrolling: 'enabled'
  })],
  exports: [RouterModule]
})
export class AppRoutingModule {}
