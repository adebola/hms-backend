-- V8: Link OAuth2 Registered Clients to Tenants
-- This migration enables multi-tenant OAuth2 client management

-- Add tenant_id to oauth2_registered_client table
ALTER TABLE oauth2_registered_client
ADD COLUMN IF NOT EXISTS tenant_id UUID;

-- Add foreign key to tenants table
-- Cascade delete: if tenant is deleted, their OAuth clients are also deleted
ALTER TABLE oauth2_registered_client
ADD CONSTRAINT fk_oauth_client_tenant
FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_oauth_client_tenant_id
ON oauth2_registered_client(tenant_id);

-- Create composite unique index
-- This allows reusing client ID patterns like "web-client" per tenant
-- Each tenant can have a "hospitalA-web-client", "hospitalB-web-client", etc.
CREATE UNIQUE INDEX IF NOT EXISTS idx_oauth_client_id_tenant
ON oauth2_registered_client(client_id, COALESCE(tenant_id, '00000000-0000-0000-0000-000000000000'::uuid));

-- Add audit columns
ALTER TABLE oauth2_registered_client
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE oauth2_registered_client
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE oauth2_registered_client
ADD COLUMN IF NOT EXISTS created_by VARCHAR(100);

-- Add status column for client lifecycle management
ALTER TABLE oauth2_registered_client
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';

-- Add constraint for status values
ALTER TABLE oauth2_registered_client
ADD CONSTRAINT chk_oauth_client_status
CHECK (status IN ('ACTIVE', 'SUSPENDED', 'REVOKED'));

-- Comments for documentation
COMMENT ON COLUMN oauth2_registered_client.tenant_id IS 'Links OAuth client to specific tenant (NULL for system clients)';
COMMENT ON COLUMN oauth2_registered_client.status IS 'Client status: ACTIVE, SUSPENDED, REVOKED';
COMMENT ON COLUMN oauth2_registered_client.created_by IS 'Username of user who created this client';
COMMENT ON COLUMN oauth2_registered_client.created_at IS 'Timestamp when client was created';
COMMENT ON COLUMN oauth2_registered_client.updated_at IS 'Timestamp when client was last updated';

-- Create function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_oauth_client_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to call the function
DROP TRIGGER IF EXISTS trg_oauth_client_updated_at ON oauth2_registered_client;
CREATE TRIGGER trg_oauth_client_updated_at
    BEFORE UPDATE ON oauth2_registered_client
    FOR EACH ROW
    EXECUTE FUNCTION update_oauth_client_updated_at();
