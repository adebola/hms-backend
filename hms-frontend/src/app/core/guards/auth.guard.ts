import { inject } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Authentication Guard
 * Prevents unauthorized access to protected routes
 */
export const authGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // Store the attempted URL for redirecting after login
  router.navigate(['/login'], {
    queryParams: { returnUrl: state.url }
  });

  return false;
};

/**
 * Role Guard
 * Checks if user has required role(s) to access a route
 * Usage in routes: canActivate: [roleGuard], data: { roles: ['ADMIN', 'DOCTOR'] }
 */
export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  const requiredRoles = route.data['roles'] as string[];
  if (!requiredRoles || requiredRoles.length === 0) {
    return true; // No specific roles required
  }

  if (authService.hasAnyRole(requiredRoles)) {
    return true;
  }

  // User doesn't have required role, redirect to unauthorized page
  router.navigate(['/unauthorized']);
  return false;
};

/**
 * Permission Guard
 * Checks if user has required permission(s) to access a route
 * Usage in routes: canActivate: [permissionGuard], data: { permissions: ['USER_CREATE', 'USER_UPDATE'] }
 */
export const permissionGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  const requiredPermissions = route.data['permissions'] as string[];
  if (!requiredPermissions || requiredPermissions.length === 0) {
    return true; // No specific permissions required
  }

  // Check if user has all required permissions
  const hasAllPermissions = requiredPermissions.every(permission =>
    authService.hasPermission(permission)
  );

  if (hasAllPermissions) {
    return true;
  }

  // User doesn't have required permission, redirect to unauthorized page
  router.navigate(['/unauthorized']);
  return false;
};

/**
 * Guest Guard
 * Prevents authenticated users from accessing guest-only routes (like login)
 */
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return true;
  }

  // User is already authenticated, redirect to dashboard
  router.navigate(['/dashboard']);
  return false;
};
