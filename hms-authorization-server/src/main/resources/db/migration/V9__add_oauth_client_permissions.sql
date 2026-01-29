-- V9: Add OAuth2 Client Management Permissions
-- These permissions control who can manage OAuth2 clients

-- Insert OAuth2 client management permissions
INSERT INTO permissions (id, code, description, resource, action)
VALUES
    (gen_random_uuid(), 'oauth_client:create', 'Create new OAuth2 clients for tenants', 'oauth_client', 'create'),
    (gen_random_uuid(), 'oauth_client:read', 'View OAuth2 client details', 'oauth_client', 'read'),
    (gen_random_uuid(), 'oauth_client:update', 'Update OAuth2 client configuration and rotate secrets', 'oauth_client', 'update'),
    (gen_random_uuid(), 'oauth_client:delete', 'Delete or revoke OAuth2 clients', 'oauth_client', 'delete')
ON CONFLICT (code) DO NOTHING;

-- Grant OAuth2 client management permissions to SUPER_ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
  AND p.resource = 'oauth_client'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Grant OAuth2 client READ permission to TENANT_ADMIN role
-- (Tenant admins can view their own clients)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'TENANT_ADMIN'
  AND p.code = 'oauth_client:read'
ON CONFLICT (role_id, permission_id) DO NOTHING;
