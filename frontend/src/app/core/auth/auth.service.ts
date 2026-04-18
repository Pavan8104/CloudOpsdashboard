import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CurrentUser, LoginRequest, LoginResponse, UserRole, isAdmin, isEngineer } from './auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private apiUrl = `${environment.apiUrl}/auth`;
  private currentUserSubject = new BehaviorSubject<CurrentUser | null>(this.loadUserFromStorage());
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap(response => {
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
      })
    );
  }

  logout(): void {
    localStorage.removeItem(environment.tokenKey);
    localStorage.removeItem(environment.userKey);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  get currentUser(): CurrentUser | null { return this.currentUserSubject.value; }
  get isLoggedIn(): boolean { return this.currentUser !== null && this.isTokenValid(); }
  getToken(): string | null { return this.currentUser?.token ?? null; }
  hasRole(role: UserRole): boolean { return this.currentUser?.roles?.includes(role) ?? false; }
  get isAdmin(): boolean { return isAdmin(this.currentUser); }
  get isEngineer(): boolean { return isEngineer(this.currentUser); }

  private isTokenValid(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

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
