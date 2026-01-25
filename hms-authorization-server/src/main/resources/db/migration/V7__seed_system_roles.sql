-- V7__seed_system_roles.sql
-- Seed system roles with permission mappings

-- Insert system roles (tenant_id is NULL for system roles)
INSERT INTO roles (id, tenant_id, code, name, description, system_role) VALUES
(gen_random_uuid(), NULL, 'SUPER_ADMIN', 'Super Administrator', 'Platform administrator with full access', true),
(gen_random_uuid(), NULL, 'TENANT_ADMIN', 'Tenant Administrator', 'Hospital administrator', true),
(gen_random_uuid(), NULL, 'DOCTOR', 'Doctor', 'Medical doctor', true),
(gen_random_uuid(), NULL, 'NURSE', 'Nurse', 'Nursing staff', true),
(gen_random_uuid(), NULL, 'RECEPTIONIST', 'Receptionist', 'Front desk staff', true),
(gen_random_uuid(), NULL, 'CASHIER', 'Cashier', 'Billing and payment staff', true),
(gen_random_uuid(), NULL, 'LAB_TECHNICIAN', 'Lab Technician', 'Laboratory staff', true),
(gen_random_uuid(), NULL, 'PHARMACIST', 'Pharmacist', 'Pharmacy staff', true),
(gen_random_uuid(), NULL, 'RECORDS_OFFICER', 'Records Officer', 'Medical records staff', true);

-- Assign all permissions to SUPER_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'SUPER_ADMIN';

-- Assign permissions to TENANT_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'TENANT_ADMIN'
AND p.code NOT IN ('tenant:activate', 'tenant:suspend');

-- Assign permissions to DOCTOR
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'DOCTOR'
AND p.code IN (
    'patient:read', 'patient:update',
    'encounter:create', 'encounter:read', 'encounter:update',
    'prescription:create', 'prescription:read', 'prescription:update',
    'lab:create', 'lab:read',
    'appointment:read', 'appointment:update',
    'report:patient'
);

-- Assign permissions to NURSE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'NURSE'
AND p.code IN (
    'patient:read', 'patient:update',
    'encounter:read', 'encounter:update',
    'prescription:read',
    'lab:read',
    'appointment:read'
);

-- Assign permissions to RECEPTIONIST
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'RECEPTIONIST'
AND p.code IN (
    'patient:create', 'patient:read', 'patient:update',
    'appointment:create', 'appointment:read', 'appointment:update', 'appointment:cancel',
    'billing:read'
);

-- Assign permissions to CASHIER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'CASHIER'
AND p.code IN (
    'patient:read',
    'billing:create', 'billing:read', 'billing:update', 'billing:payment',
    'report:financial'
);

-- Assign permissions to LAB_TECHNICIAN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'LAB_TECHNICIAN'
AND p.code IN (
    'patient:read',
    'lab:read', 'lab:update', 'lab:result'
);

-- Assign permissions to PHARMACIST
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'PHARMACIST'
AND p.code IN (
    'patient:read',
    'prescription:read', 'prescription:dispense',
    'inventory:read', 'inventory:update', 'inventory:order'
);

-- Assign permissions to RECORDS_OFFICER
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'RECORDS_OFFICER'
AND p.code IN (
    'patient:read', 'patient:update',
    'encounter:read',
    'report:patient', 'report:operational'
);
