import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../auth/auth.service';

/**
 * Auth Guard - protected routes pe jaane se pehle login check karta hai.
 *
 * Agar user logged in nahi hai toh login page pe redirect karo.
 * Saari dashboard routes pe yeh guard lagega - app-routing.module.ts mein.
 * Token expiry bhi AuthService check karta hai - expired token = not logged in.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    if (this.authService.isLoggedIn) {
      // User logged in hai - route pe jaane do
      return true;
    }

    // Logged in nahi - login page pe bhejo
    // returnUrl se baad mein jahan jaana tha wahan redirect kar sakte hain
    console.warn('AuthGuard: User not authenticated, redirecting to login');
    return this.router.createUrlTree(['/login']);
  }
}
