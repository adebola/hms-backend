-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Email messages table
CREATE TABLE email_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Email details
    to_email VARCHAR(255) NOT NULL,
    to_name VARCHAR(255),
    from_email VARCHAR(255) NOT NULL,
    from_name VARCHAR(255),

    -- Content
    subject VARCHAR(500) NOT NULL,
    html_content TEXT,
    text_content TEXT,

    -- Status tracking
    status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'BOUNCED', 'OPENED', 'CLICKED')),
    provider_id VARCHAR(255),  -- Brevo message ID

    -- Attachments (JSONB array)
    attachments JSONB,

    -- Timestamps
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    opened_at TIMESTAMP,
    clicked_at TIMESTAMP,

    -- Error handling
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    last_retry_at TIMESTAMP,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INTEGER NOT NULL DEFAULT 0
);

-- Indexes for email_messages
CREATE INDEX idx_email_messages_tenant_id ON email_messages(tenant_id);
CREATE INDEX idx_email_messages_status ON email_messages(status);
CREATE INDEX idx_email_messages_created_at ON email_messages(created_at);
CREATE INDEX idx_email_messages_provider_id ON email_messages(provider_id);
CREATE INDEX idx_email_messages_tenant_status ON email_messages(tenant_id, status);

-- SMS messages table
CREATE TABLE sms_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- SMS details
    to_phone VARCHAR(20) NOT NULL,
    from_phone VARCHAR(20),

    -- Content
    message TEXT NOT NULL,

    -- Status tracking
    status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING', 'SENT', 'DELIVERED', 'FAILED', 'BOUNCED')),
    provider_id VARCHAR(255),  -- Twilio SID

    -- Timestamps
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,

    -- Error handling
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    last_retry_at TIMESTAMP,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    version INTEGER NOT NULL DEFAULT 0
);

-- Indexes for sms_messages
CREATE INDEX idx_sms_messages_tenant_id ON sms_messages(tenant_id);
CREATE INDEX idx_sms_messages_status ON sms_messages(status);
CREATE INDEX idx_sms_messages_created_at ON sms_messages(created_at);
CREATE INDEX idx_sms_messages_provider_id ON sms_messages(provider_id);
CREATE INDEX idx_sms_messages_tenant_status ON sms_messages(tenant_id, status);

-- Update trigger function for updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply update triggers
CREATE TRIGGER email_messages_updated_at
    BEFORE UPDATE ON email_messages
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER sms_messages_updated_at
    BEFORE UPDATE ON sms_messages
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
