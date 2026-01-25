-- V2__create_user_tables.sql
-- User table with security features

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    profile_photo_url VARCHAR(500),
    title VARCHAR(50),
    specialization VARCHAR(100),
    license_number VARCHAR(50),
    department VARCHAR(100),
    
    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    
    -- MFA
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret VARCHAR(100),
    
    -- Security
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    last_login_at TIMESTAMP,
    last_login_ip VARCHAR(45),
    password_changed_at TIMESTAMP,
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    
    CONSTRAINT uk_user_tenant_username UNIQUE (tenant_id, username),
    CONSTRAINT uk_user_tenant_email UNIQUE (tenant_id, email),
    CONSTRAINT chk_user_status CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'INACTIVE', 'LOCKED', 'SUSPENDED'))
);

CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_tenant ON users(tenant_id);
CREATE INDEX idx_user_status ON users(status);

-- Password history for preventing password reuse
CREATE TABLE user_password_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_password_history_user ON user_password_history(user_id);
CREATE INDEX idx_password_history_created ON user_password_history(created_at);
