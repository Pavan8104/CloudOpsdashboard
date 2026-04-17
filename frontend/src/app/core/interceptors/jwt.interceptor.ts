import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

/**
 * JWT Interceptor - har outgoing HTTP request mein Authorization header add karta hai.
 *
 * Yeh Angular HTTP interceptor hai - middleware jaisa kaam karta hai.
 * Token hai toh automatically "Bearer <token>" header add ho jaata hai.
 * 401 response pe logout karo - token expire ya invalid ho gaya.
 * Login request pe token nahi lagate (obviously - wahan se token milta hai).
 */
@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.authService.getToken();

    // Token hai toh request clone karo header ke saath
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        // 401 - token expire ya invalid - logout karo
        if (error.status === 401) {
          console.warn('JWT Interceptor: 401 received, logging out user');
          this.authService.logout();
          // Login pe redirect already authService.logout() mein hota hai
        }

        // 403 - permission nahi hai - user ko feedback do
        if (error.status === 403) {
          console.warn('JWT Interceptor: 403 Forbidden - insufficient permissions');
          // Dashboard pe redirect kar sakte hain ya error toast dikha sakte hain
        }

        // Error aage propagate karo - component mein handle ho sake
        return throwError(() => error);
      })
    );
  }
}
