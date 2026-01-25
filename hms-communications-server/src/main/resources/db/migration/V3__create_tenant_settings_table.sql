-- Tenant settings table
CREATE TABLE tenant_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID UNIQUE NOT NULL,

    -- Email settings
    default_from_email VARCHAR(255),
    default_from_name VARCHAR(255),
    email_signature TEXT,

    -- SMS settings
    default_from_phone VARCHAR(20),

    -- Rate limits
    daily_email_limit INTEGER NOT NULL DEFAULT 1000,
    daily_sms_limit INTEGER NOT NULL DEFAULT 100,

    -- Current counters
    emails_sent_today INTEGER NOT NULL DEFAULT 0,
    sms_sent_today INTEGER NOT NULL DEFAULT 0,
    limit_reset_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Tracking settings
    enable_open_tracking BOOLEAN NOT NULL DEFAULT true,
    enable_click_tracking BOOLEAN NOT NULL DEFAULT true,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INTEGER NOT NULL DEFAULT 0
);

-- Indexes for tenant_settings
CREATE INDEX idx_tenant_settings_tenant_id ON tenant_settings(tenant_id);

-- Update trigger for tenant_settings
CREATE TRIGGER tenant_settings_updated_at
    BEFORE UPDATE ON tenant_settings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add comments for documentation
COMMENT ON TABLE tenant_settings IS 'Communication settings and rate limits for each tenant';
COMMENT ON COLUMN tenant_settings.daily_email_limit IS 'Maximum number of emails per day per tenant';
COMMENT ON COLUMN tenant_settings.daily_sms_limit IS 'Maximum number of SMS per day per tenant';
COMMENT ON COLUMN tenant_settings.limit_reset_date IS 'Date when daily counters were last reset';
COMMENT ON COLUMN tenant_settings.enable_open_tracking IS 'Enable tracking when emails are opened';
COMMENT ON COLUMN tenant_settings.enable_click_tracking IS 'Enable tracking when links in emails are clicked';
