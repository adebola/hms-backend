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
  tenantBranding$ = this.tenantService.tenantBranding$;
  loading = false;

  constructor(
    private authService: AuthService,
    private tenantService: TenantService,
    private router: Router
  ) {}

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
}
