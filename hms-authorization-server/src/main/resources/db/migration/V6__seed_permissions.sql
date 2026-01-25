-- V6__seed_permissions.sql
-- Seed system permissions

INSERT INTO permissions (id, code, resource, action, description) VALUES
-- Patient permissions
(gen_random_uuid(), 'patient:create', 'patient', 'create', 'Create new patient records'),
(gen_random_uuid(), 'patient:read', 'patient', 'read', 'View patient records'),
(gen_random_uuid(), 'patient:update', 'patient', 'update', 'Update patient records'),
(gen_random_uuid(), 'patient:delete', 'patient', 'delete', 'Delete patient records'),

-- Encounter permissions
(gen_random_uuid(), 'encounter:create', 'encounter', 'create', 'Create patient encounters'),
(gen_random_uuid(), 'encounter:read', 'encounter', 'read', 'View patient encounters'),
(gen_random_uuid(), 'encounter:update', 'encounter', 'update', 'Update patient encounters'),
(gen_random_uuid(), 'encounter:delete', 'encounter', 'delete', 'Delete patient encounters'),

-- Prescription permissions
(gen_random_uuid(), 'prescription:create', 'prescription', 'create', 'Create prescriptions'),
(gen_random_uuid(), 'prescription:read', 'prescription', 'read', 'View prescriptions'),
(gen_random_uuid(), 'prescription:update', 'prescription', 'update', 'Update prescriptions'),
(gen_random_uuid(), 'prescription:delete', 'prescription', 'delete', 'Delete prescriptions'),
(gen_random_uuid(), 'prescription:dispense', 'prescription', 'dispense', 'Dispense medications'),

-- Lab permissions
(gen_random_uuid(), 'lab:create', 'lab', 'create', 'Create lab orders'),
(gen_random_uuid(), 'lab:read', 'lab', 'read', 'View lab orders and results'),
(gen_random_uuid(), 'lab:update', 'lab', 'update', 'Update lab orders'),
(gen_random_uuid(), 'lab:result', 'lab', 'result', 'Enter lab results'),

-- Billing permissions
(gen_random_uuid(), 'billing:create', 'billing', 'create', 'Create invoices'),
(gen_random_uuid(), 'billing:read', 'billing', 'read', 'View billing records'),
(gen_random_uuid(), 'billing:update', 'billing', 'update', 'Update billing records'),
(gen_random_uuid(), 'billing:payment', 'billing', 'payment', 'Process payments'),
(gen_random_uuid(), 'billing:refund', 'billing', 'refund', 'Process refunds'),

-- Appointment permissions
(gen_random_uuid(), 'appointment:create', 'appointment', 'create', 'Schedule appointments'),
(gen_random_uuid(), 'appointment:read', 'appointment', 'read', 'View appointments'),
(gen_random_uuid(), 'appointment:update', 'appointment', 'update', 'Update appointments'),
(gen_random_uuid(), 'appointment:cancel', 'appointment', 'cancel', 'Cancel appointments'),

-- User management permissions
(gen_random_uuid(), 'user:create', 'user', 'create', 'Create users'),
(gen_random_uuid(), 'user:read', 'user', 'read', 'View users'),
(gen_random_uuid(), 'user:update', 'user', 'update', 'Update users'),
(gen_random_uuid(), 'user:delete', 'user', 'delete', 'Delete users'),
(gen_random_uuid(), 'user:deactivate', 'user', 'deactivate', 'Deactivate users'),

-- Role management permissions
(gen_random_uuid(), 'role:create', 'role', 'create', 'Create roles'),
(gen_random_uuid(), 'role:read', 'role', 'read', 'View roles'),
(gen_random_uuid(), 'role:update', 'role', 'update', 'Update roles'),
(gen_random_uuid(), 'role:delete', 'role', 'delete', 'Delete roles'),

-- Permission management
(gen_random_uuid(), 'permission:read', 'permission', 'read', 'View permissions'),

-- Tenant management permissions
(gen_random_uuid(), 'tenant:read', 'tenant', 'read', 'View tenant details'),
(gen_random_uuid(), 'tenant:update', 'tenant', 'update', 'Update tenant settings'),
(gen_random_uuid(), 'tenant:activate', 'tenant', 'activate', 'Activate tenants'),
(gen_random_uuid(), 'tenant:suspend', 'tenant', 'suspend', 'Suspend tenants'),

-- Report permissions
(gen_random_uuid(), 'report:patient', 'report', 'patient', 'Generate patient reports'),
(gen_random_uuid(), 'report:financial', 'report', 'financial', 'Generate financial reports'),
(gen_random_uuid(), 'report:operational', 'report', 'operational', 'Generate operational reports'),

-- Inventory permissions
(gen_random_uuid(), 'inventory:read', 'inventory', 'read', 'View inventory'),
(gen_random_uuid(), 'inventory:update', 'inventory', 'update', 'Update inventory'),
(gen_random_uuid(), 'inventory:order', 'inventory', 'order', 'Place inventory orders'),

-- Settings permissions
(gen_random_uuid(), 'settings:read', 'settings', 'read', 'View system settings'),
(gen_random_uuid(), 'settings:update', 'settings', 'update', 'Update system settings');
