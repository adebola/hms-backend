/**
 * User-related data models
 */

export interface User {
  id: string;
  tenantId: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  status: UserStatus;
  roles: Role[];
  createdAt: string;
  updatedAt: string;
  lastLoginAt?: string;
}

export interface Role {
  id: string;
  name: string;
  description?: string;
  systemRole: boolean;
  permissions: Permission[];
}

export interface Permission {
  id: string;
  name: string;
  description?: string;
  resource: string;
  action: string;
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  LOCKED = 'LOCKED',
  PENDING_ACTIVATION = 'PENDING_ACTIVATION',
  SUSPENDED = 'SUSPENDED'
}

export interface CreateUserRequest {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  password: string;
  roleIds: string[];
}
