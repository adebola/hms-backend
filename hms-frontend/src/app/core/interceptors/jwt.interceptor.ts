import { HttpInterceptorFn, HttpErrorResponse, HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { environment } from '@environments/environment';

/**
 * JWT Interceptor
 * Automatically adds Authorization header with JWT token to outgoing requests
 * Handles token refresh on 401 errors
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  // Skip interceptor for login and refresh endpoints
  if (req.url.includes('/auth/login') || req.url.includes('/auth/refresh')) {
    return next(req);
  }

  // Get access token
  const token = authService.getAccessToken();

  // Clone request and add Authorization header if token exists
  let authReq = req;
  if (token) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });

    // Log request in debug mode
    if (environment.enableDebugMode) {
      console.log(`[HTTP] ${req.method} ${req.url}`);
    }
  }

  // Handle the request and catch 401 errors for token refresh
  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // If 401 Unauthorized and we have a refresh token, try to refresh
      if (error.status === 401 && authService.getRefreshToken()) {
        return handleTokenRefresh(authService, req, next);
      }

      // For other errors, just pass them through
      return throwError(() => error);
    })
  );
};

/**
 * Handle token refresh and retry the original request
 */
function handleTokenRefresh(
  authService: AuthService,
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
): Observable<HttpEvent<unknown>> {
  return authService.refreshToken().pipe(
    switchMap(() => {
      // Get the new token
      const newToken = authService.getAccessToken();

      // Clone the original request with new token
      const retryReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${newToken}`
        }
      });

      // Retry the original request
      if (environment.enableDebugMode) {
        console.log('[HTTP] Retrying request with refreshed token');
      }

      return next(retryReq);
    }),
    catchError((refreshError) => {
      // If refresh fails, logout and redirect to login
      if (environment.enableDebugMode) {
        console.error('[HTTP] Token refresh failed, logging out');
      }
      // authService.logout() is called inside refreshToken on error
      return throwError(() => refreshError);
    })
  );
}

/**
 * Error Interceptor
 * Handles HTTP errors and provides user-friendly error messages
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unknown error occurred';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Error: ${error.error.message}`;
      } else {
        // Server-side error
        if (error.status === 0) {
          errorMessage = 'Unable to connect to server. Please check your internet connection.';
        } else if (error.error?.message) {
          errorMessage = error.error.message;
        } else {
          errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
        }
      }

      if (environment.enableDebugMode) {
        console.error('[HTTP Error]', {
          url: req.url,
          status: error.status,
          message: errorMessage,
          error: error
        });
      }

      return throwError(() => ({
        ...error,
        userMessage: errorMessage
      }));
    })
  );
};

/**
 * Request ID Interceptor
 * Adds a unique request ID to each request for tracking
 */
export const requestIdInterceptor: HttpInterceptorFn = (req, next) => {
  const requestId = generateRequestId();

  const reqWithId = req.clone({
    setHeaders: {
      'X-Request-ID': requestId
    }
  });

  return next(reqWithId);
};

/**
 * Generate unique request ID
 */
function generateRequestId(): string {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
}
