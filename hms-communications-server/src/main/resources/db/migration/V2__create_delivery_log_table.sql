-- Delivery logs table
CREATE TABLE delivery_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL,
    message_type VARCHAR(20) NOT NULL CHECK (message_type IN ('EMAIL', 'SMS')),
    tenant_id UUID NOT NULL,

    -- Event details
    event_type VARCHAR(50) NOT NULL,  -- sent, delivered, bounced, opened, clicked, etc.
    event_data JSONB,

    -- Provider information
    provider_name VARCHAR(50) NOT NULL,  -- brevo, twilio
    provider_response JSONB,

    -- Timestamps
    occurred_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for delivery_logs
CREATE INDEX idx_delivery_logs_message_id ON delivery_logs(message_id);
CREATE INDEX idx_delivery_logs_tenant_id ON delivery_logs(tenant_id);
CREATE INDEX idx_delivery_logs_event_type ON delivery_logs(event_type);
CREATE INDEX idx_delivery_logs_occurred_at ON delivery_logs(occurred_at);
CREATE INDEX idx_delivery_logs_message_type ON delivery_logs(message_type);
CREATE INDEX idx_delivery_logs_tenant_event ON delivery_logs(tenant_id, event_type);

-- Add comments for documentation
COMMENT ON TABLE delivery_logs IS 'Tracks all delivery events for email and SMS messages';
COMMENT ON COLUMN delivery_logs.event_type IS 'Event types: sent, delivered, bounced, opened, clicked, failed, etc.';
COMMENT ON COLUMN delivery_logs.event_data IS 'Additional event-specific data in JSON format';
COMMENT ON COLUMN delivery_logs.provider_response IS 'Raw response from the messaging provider';
