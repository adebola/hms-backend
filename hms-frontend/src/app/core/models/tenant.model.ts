/**
 * Tenant-related data models
 */

export interface Tenant {
  id: string;
  tenantCode: string;
  facilityName: string;
  facilityType: FacilityType;
  facilityLevel: FacilityLevel;
  address: Address;
  contactPerson: string;
  contactEmail: string;
  contactPhone: string;
  subscriptionPlan: SubscriptionPlan;
  status: TenantStatus;
  createdAt: string;
  updatedAt: string;
  activatedAt?: string;
  deactivatedAt?: string;
}

export interface Address {
  street: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
}

export enum FacilityType {
  HOSPITAL = 'HOSPITAL',
  CLINIC = 'CLINIC',
  HEALTH_CENTER = 'HEALTH_CENTER',
  DIAGNOSTIC_CENTER = 'DIAGNOSTIC_CENTER',
  PHARMACY = 'PHARMACY',
  REHABILITATION_CENTER = 'REHABILITATION_CENTER'
}

export enum FacilityLevel {
  PRIMARY = 'PRIMARY',
  SECONDARY = 'SECONDARY',
  TERTIARY = 'TERTIARY',
  QUATERNARY = 'QUATERNARY'
}

export enum SubscriptionPlan {
  FREE = 'FREE',
  BASIC = 'BASIC',
  PROFESSIONAL = 'PROFESSIONAL',
  ENTERPRISE = 'ENTERPRISE'
}

export enum TenantStatus {
  PENDING = 'PENDING',
  ACTIVE = 'ACTIVE',
  SUSPENDED = 'SUSPENDED',
  DEACTIVATED = 'DEACTIVATED'
}

export interface TenantBranding {
  tenantCode: string;
  facilityName: string;
  logoUrl?: string;
  primaryColor?: string;
  secondaryColor?: string;
  accentColor?: string;
  customStyles?: { [key: string]: string };
}

export interface TenantRegistrationRequest {
  tenantCode: string;
  facilityName: string;
  facilityType: FacilityType;
  facilityLevel: FacilityLevel;
  address: Address;
  contactPerson: string;
  contactEmail: string;
  contactPhone: string;
  subscriptionPlan: SubscriptionPlan;
  adminUser: {
    username: string;
    email: string;
    firstName: string;
    lastName: string;
    password: string;
  };
}
