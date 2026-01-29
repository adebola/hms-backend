import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { TenantService } from '@core/services/tenant.service';
import { environment } from '@environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  loading = false;
  errorMessage = '';
  returnUrl = '/dashboard';
  tenantBranding$;

  // Tenant code from environment (not user input)
  private readonly tenantCode: string;

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private tenantService: TenantService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.tenantBranding$ = this.tenantService.tenantBranding$;
    // Resolve tenant code from environment or subdomain
    this.tenantCode = this.resolveTenantCode();
  }

  ngOnInit(): void {
    // Get return URL from route parameters or default to dashboard
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';

    // Initialize login form (only username and password - no tenant code)
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });

    // Load tenant branding based on configured tenant
    this.loadTenantBranding(this.tenantCode);
  }

  /**
   * Resolve tenant code from environment or subdomain
   */
  private resolveTenantCode(): string {
    // Try subdomain detection first (if enabled)
    if (environment.tenant.autoDetectFromSubdomain) {
      const subdomain = this.tenantService.extractTenantFromSubdomain();
      if (subdomain) {
        return subdomain;
      }
    }

    // Fall back to environment configuration
    return environment.tenant.code;
  }

  /**
   * Load tenant branding
   */
  private loadTenantBranding(tenantCode: string): void {
    this.tenantService.getTenantBranding(tenantCode).subscribe({
      error: (error) => {
        console.warn('Failed to load tenant branding:', error);
        // Continue with login even if branding fails
      }
    });
  }

  /**
   * Handle login form submission
   */
  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.markFormGroupTouched(this.loginForm);
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const { username, password } = this.loginForm.value;

    // Login with pre-configured tenant code
    this.authService.login(this.tenantCode, username, password).subscribe({
      next: () => {
        // Store tenant code for future use
        this.tenantService.setTenantCode(this.tenantCode);

        // Navigate to return URL or dashboard
        this.router.navigate([this.returnUrl]);
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = this.getErrorMessage(error);
        console.error('Login error:', error);
      }
    });
  }

  /**
   * Extract user-friendly error message
   */
  private getErrorMessage(error: any): string {
    if (error.userMessage) {
      return error.userMessage;
    }

    if (error.error?.message) {
      return error.error.message;
    }

    if (error.status === 401) {
      return 'Invalid credentials. Please check your tenant code, username, and password.';
    }

    if (error.status === 0) {
      return 'Unable to connect to server. Please check your internet connection.';
    }

    return 'Login failed. Please try again.';
  }

  /**
   * Mark all form controls as touched to show validation errors
   */
  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();
    });
  }

  /**
   * Check if form field has error
   */
  hasError(fieldName: string, errorCode: string): boolean {
    const field = this.loginForm.get(fieldName);
    return !!(field && field.hasError(errorCode) && field.touched);
  }

  /**
   * Clear error message
   */
  clearError(): void {
    this.errorMessage = '';
  }
}
