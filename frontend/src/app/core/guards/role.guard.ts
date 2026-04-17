import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router,
  UrlTree
} from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { UserRole } from '../auth/auth.model';

/**
 * Role Guard - specific roles wale users hi kuch routes pe ja sakte hain.
 *
 * Route data mein 'roles' array define karo - guard check karega.
 * Example usage in routing:
 * { path: 'admin', component: AdminComponent, data: { roles: ['ROLE_ADMIN'] } }
 *
 * Unauthorized hone pe dashboard pe redirect karo (forbidden page nahi - better UX).
 */
@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    // Pehle logged in check karo
    if (!this.authService.isLoggedIn) {
      return this.router.createUrlTree(['/login']);
    }

    // Route mein required roles define hain
    const requiredRoles = route.data['roles'] as UserRole[];

    // Roles define nahi kiye toh sab logged in users ke liye allow
    if (!requiredRoles || requiredRoles.length === 0) {
      return true;
    }

    // User ke paas koi bhi required role hai?
    const hasRequiredRole = requiredRoles.some(role =>
      this.authService.hasRole(role)
    );

    if (hasRequiredRole) {
      return true;
    }

    // Role nahi hai - dashboard pe wapas bhejo, forbidden page dikhaoge
    console.warn('RoleGuard: Insufficient permissions for route:', route.url);
    return this.router.createUrlTree(['/dashboard']);
  }
}
