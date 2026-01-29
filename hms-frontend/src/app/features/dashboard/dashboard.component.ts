import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { TenantService } from '@core/services/tenant.service';
import { User } from '@core/models/user.model';
import { DecodedToken } from '@core/models/auth.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  currentUser: User | null = null;
  tokenInfo: DecodedToken | null = null;
  tenantBranding$;
  loading = false;

  constructor(
    private authService: AuthService,
    private tenantService: TenantService,
    private router: Router
  ) {
    this.tenantBranding$ = this.tenantService.tenantBranding$;
  }

  ngOnInit(): void {
    this.loadUserInfo();
    this.tokenInfo = this.authService.getDecodedToken();
  }

  /**
   * Load current user information
   */
  private loadUserInfo(): void {
    this.loading = true;
    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load user info:', error);
        this.loading = false;
      }
    });
  }

  /**
   * Handle logout
   */
  logout(): void {
    if (confirm('Are you sure you want to logout?')) {
      this.authService.logout().subscribe({
        next: () => {
          this.router.navigate(['/login']);
        },
        error: (error) => {
          console.error('Logout error:', error);
          // Navigate to login anyway
          this.router.navigate(['/login']);
        }
      });
    }
  }

  /**
   * Get user's full name
   */
  get fullName(): string {
    if (!this.currentUser) return 'User';
    return `${this.currentUser.firstName} ${this.currentUser.lastName}`;
  }

  /**
   * Get user's primary role
   */
  get primaryRole(): string {
    if (!this.currentUser || !this.currentUser.roles || this.currentUser.roles.length === 0) {
      return 'No Role';
    }
    return this.currentUser.roles[0].name;
  }

  /**
   * Format date
   */
  formatDate(dateString: string | undefined): string {
    if (!dateString) return 'Never';
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
  }

  /**
   * Format timestamp (seconds since epoch) to date string
   */
  formatTimestamp(timestamp: number | undefined): string {
    if (!timestamp) return 'N/A';
    const date = new Date(timestamp * 1000);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
  }

  /**
   * Get permissions as array (handles string or array format from JWT)
   */
  get permissions(): string[] {
    if (!this.tokenInfo?.permissions) return [];

    // If permissions is already an array, return it
    if (Array.isArray(this.tokenInfo.permissions)) {
      return this.tokenInfo.permissions;
    }

    // If permissions is a string, split by space or comma
    if (typeof this.tokenInfo.permissions === 'string') {
      return this.tokenInfo.permissions.split(/[\s,]+/).filter(p => p.length > 0);
    }

    return [];
  }

  /**
   * Get permissions count
   */
  get permissionsCount(): number {
    return this.permissions.length;
  }
}
