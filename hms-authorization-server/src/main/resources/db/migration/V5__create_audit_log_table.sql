-- V5__create_audit_log_table.sql
-- Authentication audit log

CREATE TABLE auth_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    user_id UUID,
    username VARCHAR(100),
    event_type VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    success BOOLEAN NOT NULL,
    failure_reason VARCHAR(255),
    details JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_tenant ON auth_audit_log(tenant_id);
CREATE INDEX idx_audit_user ON auth_audit_log(user_id);
CREATE INDEX idx_audit_event_type ON auth_audit_log(event_type);
CREATE INDEX idx_audit_created ON auth_audit_log(created_at);
CREATE INDEX idx_audit_success ON auth_audit_log(success);
