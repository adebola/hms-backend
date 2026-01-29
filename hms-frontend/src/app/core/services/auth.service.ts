import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import {
  LoginRequest,
  LoginResponse,
  RefreshTokenRequest,
  RefreshTokenResponse,
  ChangePasswordRequest,
  DecodedToken
} from '../models/auth.model';
import { User } from '../models/user.model';

/**
 * Authentication service
 * Handles login, logout, token management, and user session
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly ACCESS_TOKEN_KEY = 'hms_access_token';
  private readonly REFRESH_TOKEN_KEY = 'hms_refresh_token';
  private readonly TOKEN_EXPIRY_KEY = 'hms_token_expiry';

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasValidToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(
    private apiService: ApiService,
    private router: Router
  ) {
    // Check token validity on service initialization
    this.checkTokenValidity();
  }

  /**
   * User login
   */
  login(tenantCode: string, username: string, password: string): Observable<LoginResponse> {
    const request: LoginRequest = { tenantCode, username, password };

    return this.apiService.post<LoginResponse>('auth/login', request).pipe(
      tap(response => this.handleAuthenticationSuccess(response)),
      catchError(error => this.handleAuthenticationError(error))
    );
  }

  /**
   * Refresh access token
   */
  refreshToken(): Observable<RefreshTokenResponse> {
    const refreshToken = this.getRefreshToken();

    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }

    const request: RefreshTokenRequest = { refreshToken };

    return this.apiService.post<RefreshTokenResponse>('auth/refresh', request).pipe(
      tap(response => this.handleAuthenticationSuccess(response)),
      catchError(error => {
        this.logout();
        return throwError(() => error);
      })
    );
  }

  /**
   * User logout
   */
  logout(): Observable<any> {
    return this.apiService.post('auth/logout').pipe(
      tap(() => this.clearSession()),
      catchError(() => {
        // Clear session even if API call fails
        this.clearSession();
        return throwError(() => new Error('Logout failed'));
      })
    );
  }

  /**
   * Change password
   */
  changePassword(oldPassword: string, newPassword: string): Observable<any> {
    const request: ChangePasswordRequest = { oldPassword, newPassword };
    return this.apiService.post('auth/change-password', request);
  }

  /**
   * Get current user info
   */
  getCurrentUser(): Observable<User> {
    return this.apiService.get<User>('auth/me').pipe(
      tap(user => this.currentUserSubject.next(user))
    );
  }

  /**
   * Get access token
   */
  getAccessToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  /**
   * Get refresh token
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return this.hasValidToken();
  }

  /**
   * Decode JWT token
   */
  decodeToken(token: string): DecodedToken | null {
    try {
      const payload = token.split('.')[1];
      const decoded = atob(payload);
      return JSON.parse(decoded) as DecodedToken;
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  }

  /**
   * Get decoded token info
   */
  getDecodedToken(): DecodedToken | null {
    const token = this.getAccessToken();
    return token ? this.decodeToken(token) : null;
  }

  /**
   * Check if token is expired
   */
  isTokenExpired(): boolean {
    const expiryTime = localStorage.getItem(this.TOKEN_EXPIRY_KEY);
    if (!expiryTime) return true;

    const expiry = parseInt(expiryTime, 10);
    return Date.now() >= expiry;
  }

  /**
   * Get time until token expires (in milliseconds)
   */
  getTimeUntilExpiry(): number {
    const expiryTime = localStorage.getItem(this.TOKEN_EXPIRY_KEY);
    if (!expiryTime) return 0;

    const expiry = parseInt(expiryTime, 10);
    return Math.max(0, expiry - Date.now());
  }

  /**
   * Handle successful authentication
   */
  private handleAuthenticationSuccess(response: LoginResponse | RefreshTokenResponse): void {
    // Store tokens
    localStorage.setItem(this.ACCESS_TOKEN_KEY, response.accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);

    // Calculate and store expiry time (expiresIn is in seconds)
    const expiryTime = Date.now() + (response.expiresIn * 1000);
    localStorage.setItem(this.TOKEN_EXPIRY_KEY, expiryTime.toString());

    // Update authentication state
    this.isAuthenticatedSubject.next(true);

    // Fetch current user info
    this.getCurrentUser().subscribe({
      error: (error) => console.error('Failed to fetch user info:', error)
    });
  }

  /**
   * Handle authentication error
   */
  private handleAuthenticationError(error: any): Observable<never> {
    console.error('Authentication error:', error);
    this.clearSession();
    return throwError(() => error);
  }

  /**
   * Clear session data
   */
  private clearSession(): void {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.TOKEN_EXPIRY_KEY);

    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);

    this.router.navigate(['/login']);
  }

  /**
   * Check if valid token exists
   */
  private hasValidToken(): boolean {
    const token = this.getAccessToken();
    return token !== null && !this.isTokenExpired();
  }

  /**
   * Check token validity and refresh if needed
   */
  private checkTokenValidity(): void {
    if (!this.hasValidToken()) {
      this.clearSession();
    }
  }

  /**
   * Check if user has specific permission
   */
  hasPermission(permission: string): boolean {
    const decoded = this.getDecodedToken();
    if (!decoded?.permissions) return false;

    // Handle both array and string formats
    if (Array.isArray(decoded.permissions)) {
      return decoded.permissions.includes(permission);
    }

    if (typeof decoded.permissions === 'string') {
      return decoded.permissions.split(/[\s,]+/).includes(permission);
    }

    return false;
  }

  /**
   * Check if user has specific role
   */
  hasRole(role: string): boolean {
    const decoded = this.getDecodedToken();
    return decoded?.roles?.includes(role) ?? false;
  }

  /**
   * Check if user has any of the specified roles
   */
  hasAnyRole(roles: string[]): boolean {
    return roles.some(role => this.hasRole(role));
  }

  /**
   * Get current tenant code
   */
  getTenantCode(): string | null {
    const decoded = this.getDecodedToken();
    return decoded?.tenant_code ?? null;
  }

  /**
   * Get current tenant ID
   */
  getTenantId(): string | null {
    const decoded = this.getDecodedToken();
    return decoded?.tenant_id ?? null;
  }
}
