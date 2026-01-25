import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { Tenant, TenantBranding } from '../models/tenant.model';

/**
 * Tenant service
 * Handles tenant configuration, branding, and multi-tenancy features
 */
@Injectable({
  providedIn: 'root'
})
export class TenantService {
  private readonly TENANT_CODE_KEY = 'hms_tenant_code';
  private readonly TENANT_BRANDING_KEY = 'hms_tenant_branding';

  private currentTenantSubject = new BehaviorSubject<Tenant | null>(null);
  public currentTenant$ = this.currentTenantSubject.asObservable();

  private tenantBrandingSubject = new BehaviorSubject<TenantBranding | null>(this.getCachedBranding());
  public tenantBranding$ = this.tenantBrandingSubject.asObservable();

  constructor(private apiService: ApiService) {}

  /**
   * Get tenant by code
   */
  getTenantByCode(tenantCode: string): Observable<Tenant> {
    return this.apiService.get<Tenant>(`tenants/code/${tenantCode}`).pipe(
      tap(tenant => this.currentTenantSubject.next(tenant))
    );
  }

  /**
   * Get tenant branding by code
   */
  getTenantBranding(tenantCode: string): Observable<TenantBranding> {
    return this.apiService.get<TenantBranding>(`tenants/${tenantCode}/branding`).pipe(
      tap(branding => {
        this.tenantBrandingSubject.next(branding);
        this.cacheBranding(branding);
        this.applyBranding(branding);
      })
    );
  }

  /**
   * Set current tenant code
   */
  setTenantCode(tenantCode: string): void {
    localStorage.setItem(this.TENANT_CODE_KEY, tenantCode);
  }

  /**
   * Get current tenant code
   */
  getTenantCode(): string | null {
    return localStorage.getItem(this.TENANT_CODE_KEY);
  }

  /**
   * Clear tenant data
   */
  clearTenantData(): void {
    localStorage.removeItem(this.TENANT_CODE_KEY);
    localStorage.removeItem(this.TENANT_BRANDING_KEY);
    this.currentTenantSubject.next(null);
    this.tenantBrandingSubject.next(null);
    this.removeBranding();
  }

  /**
   * Cache branding in localStorage
   */
  private cacheBranding(branding: TenantBranding): void {
    localStorage.setItem(this.TENANT_BRANDING_KEY, JSON.stringify(branding));
  }

  /**
   * Get cached branding from localStorage
   */
  private getCachedBranding(): TenantBranding | null {
    const cached = localStorage.getItem(this.TENANT_BRANDING_KEY);
    return cached ? JSON.parse(cached) : null;
  }

  /**
   * Apply tenant branding to the application
   */
  private applyBranding(branding: TenantBranding): void {
    const root = document.documentElement;

    // Apply CSS custom properties for theming
    if (branding.primaryColor) {
      root.style.setProperty('--primary-color', branding.primaryColor);
    }
    if (branding.secondaryColor) {
      root.style.setProperty('--secondary-color', branding.secondaryColor);
    }
    if (branding.accentColor) {
      root.style.setProperty('--accent-color', branding.accentColor);
    }

    // Apply custom styles if provided
    if (branding.customStyles) {
      Object.entries(branding.customStyles).forEach(([key, value]) => {
        root.style.setProperty(`--${key}`, value);
      });
    }

    // Update page title with facility name
    document.title = `${branding.facilityName} - HMS`;

    // Update favicon if logo URL is provided (optional)
    if (branding.logoUrl) {
      const favicon = document.querySelector('link[rel="icon"]') as HTMLLinkElement;
      if (favicon) {
        favicon.href = branding.logoUrl;
      }
    }
  }

  /**
   * Remove applied branding
   */
  private removeBranding(): void {
    const root = document.documentElement;
    root.style.removeProperty('--primary-color');
    root.style.removeProperty('--secondary-color');
    root.style.removeProperty('--accent-color');
    document.title = 'HMS - Health Management System';
  }

  /**
   * Load tenant branding on app initialization
   */
  loadBrandingForCurrentTenant(): void {
    const tenantCode = this.getTenantCode();
    if (tenantCode) {
      this.getTenantBranding(tenantCode).subscribe({
        error: (error) => {
          console.error('Failed to load tenant branding:', error);
          // Use cached branding if available
          const cached = this.getCachedBranding();
          if (cached) {
            this.applyBranding(cached);
          }
        }
      });
    }
  }

  /**
   * Extract tenant code from subdomain (for production)
   * Example: hospitalA.hms.com -> HOSPITAL_A
   */
  extractTenantFromSubdomain(): string | null {
    const hostname = window.location.hostname;
    const parts = hostname.split('.');

    // If subdomain exists and it's not 'www'
    if (parts.length > 2 && parts[0] !== 'www') {
      return parts[0].toUpperCase();
    }

    return null;
  }

  /**
   * Extract tenant code from URL parameter
   * Example: app.hms.com?tenant=HOSPITAL_A
   */
  extractTenantFromUrlParam(): string | null {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('tenant');
  }

  /**
   * Determine tenant code from various sources
   * Priority: URL param > Subdomain > Cached > null
   */
  determineTenantCode(): string | null {
    return this.extractTenantFromUrlParam()
      || this.extractTenantFromSubdomain()
      || this.getTenantCode();
  }
}
