import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CurrentUser,
  LoginRequest,
  LoginResponse,
  UserRole,
  isAdmin,
  isEngineer
} from './auth.model';

/**
 * Auth Service - login, logout, aur current user state manage karta hai.
 *
 * BehaviorSubject use kiya hai currentUser ke liye - reactive hai, subscribe kar sakte hain.
 * Token localStorage mein store hota hai - JWT stateless hai, server pe kuch nahi rakhna.
 * Guards yahi service use karte hain route protection ke liye.
 *
 * Future enhancement: refresh token mechanism add karna hai - abhi sirf basic hai.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = `${environment.apiUrl}/auth`;

  // BehaviorSubject - current value plus future changes milti hain subscribers ko
  // null matlab koi logged in nahi hai
  private currentUserSubject = new BehaviorSubject<CurrentUser | null>(this.loadUserFromStorage());
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  /**
   * Login API call - credentials bhejo, JWT wapas aata hai.
   * tap() operator se side effect (storage mein save) handle karte hain.
   */
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
        // Token aur user info store karo
        const currentUser: CurrentUser = {
          userId: response.userId,
          username: response.username,
          email: response.email,
          fullName: response.fullName,
          roles: response.roles,
          token: response.accessToken
        };

        this.saveUserToStorage(currentUser);
        this.currentUserSubject.next(currentUser);

        if (environment.debug) {
          console.log('User logged in:', currentUser.username);
        }
      })
    );
  }

  /**
   * Logout - storage clear karo aur login page pe redirect karo.
   * Server pe kuch nahi karna (JWT stateless hai) but future mein blacklist add kar sakte hain.
   */
  logout(): void {
    localStorage.removeItem(environment.tokenKey);
    localStorage.removeItem(environment.userKey);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  /**
   * Current logged in user - snapshot chahiye toh yeh use karo (non-reactive).
   */
  get currentUser(): CurrentUser | null {
    return this.currentUserSubject.value;
  }

  /**
   * Logged in hai ya nahi - guards ye check karte hain.
   */
  get isLoggedIn(): boolean {
    return this.currentUser !== null && this.isTokenValid();
  }

  /**
   * Token localStorage se nikalo - interceptor yahi use karta hai.
   */
  getToken(): string | null {
    return this.currentUser?.token ?? null;
  }

  /**
   * Role checks - template aur guards mein use hote hain.
   */
  hasRole(role: UserRole): boolean {
    return this.currentUser?.roles?.includes(role) ?? false;
  }

  get isAdmin(): boolean {
    return isAdmin(this.currentUser);
  }

  get isEngineer(): boolean {
    return isEngineer(this.currentUser);
  }

  /**
   * Token expire hua ya nahi check karo - JWT payload decode karte hain.
   * Library nahi use karte - simple base64 decode se kaam ho jaata hai.
   */
  private isTokenValid(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      // JWT ka middle part (payload) base64 encoded hai
      const payload = JSON.parse(atob(token.split('.')[1]));
      // exp field seconds mein hota hai - Date.now() milliseconds mein
      return payload.exp * 1000 > Date.now();
    } catch {
      // Token malformed hai - invalid maano
      return false;
    }
  }

  /**
   * App restart hone pe localStorage se user load karo.
   * Page refresh ke baad bhi logged in rahega user.
   */
  private loadUserFromStorage(): CurrentUser | null {
    try {
      const stored = localStorage.getItem(environment.userKey);
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  }

  private saveUserToStorage(user: CurrentUser): void {
    localStorage.setItem(environment.tokenKey, user.token);
    localStorage.setItem(environment.userKey, JSON.stringify(user));
  }
}
