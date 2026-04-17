/**
 * Auth related TypeScript interfaces - login request/response types.
 *
 * Backend ke DTOs se match karte hain - agar backend change ho toh yahan bhi update karna padega.
 * Role enum define kiya hai - string comparison se zyada safe hai.
 */

// Backend ke Role enum se match karna chahiye - case sensitive hai yeh
export type UserRole = 'ROLE_ADMIN' | 'ROLE_ENGINEER' | 'ROLE_VIEWER';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;     // "Bearer"
  expiresIn: number;     // milliseconds
  userId: number;
  username: string;
  email: string;
  fullName: string;
  roles: UserRole[];
}

// Logged in user ki info - localStorage mein store hogi
export interface CurrentUser {
  userId: number;
  username: string;
  email: string;
  fullName: string;
  roles: UserRole[];
  token: string;
}

// Helper functions - role check karne ke liye service mein use hote hain
export function hasRole(user: CurrentUser | null, role: UserRole): boolean {
  return user?.roles?.includes(role) ?? false;
}

export function isAdmin(user: CurrentUser | null): boolean {
  return hasRole(user, 'ROLE_ADMIN');
}

export function isEngineer(user: CurrentUser | null): boolean {
  return hasRole(user, 'ROLE_ENGINEER') || isAdmin(user);
}

export function isViewer(user: CurrentUser | null): boolean {
  // Viewer access sabke paas hai agar logged in hai
  return user !== null;
}
