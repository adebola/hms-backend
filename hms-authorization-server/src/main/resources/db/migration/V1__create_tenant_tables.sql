-- V1__create_tenant_tables.sql
-- Tenant table for multi-tenancy support

CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(20) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    facility_type VARCHAR(50) NOT NULL,
    facility_level VARCHAR(20),
    registration_number VARCHAR(100),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    
    -- Address fields (embedded)
    street VARCHAR(255),
    city VARCHAR(100),
    lga VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    
    -- Subscription
    subscription_plan VARCHAR(30) NOT NULL,
    subscription_start_date DATE,
    subscription_end_date DATE,
    
    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    
    -- Additional info
    logo_url VARCHAR(500),
    website VARCHAR(255),
    tax_id VARCHAR(50),
    settings JSONB,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    
    CONSTRAINT chk_tenant_status CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED', 'DEACTIVATED')),
    CONSTRAINT chk_tenant_facility_type CHECK (facility_type IN ('HOSPITAL', 'CLINIC', 'DIAGNOSTIC_CENTER', 'PHARMACY', 'MATERNITY', 'SPECIALIST_HOSPITAL', 'DENTAL_CLINIC', 'OPTICAL_CENTER', 'REHABILITATION_CENTER', 'NURSING_HOME', 'MENTAL_HEALTH_FACILITY', 'RESEARCH_INSTITUTE', 'BLOOD_BANK', 'OTHER')),
    CONSTRAINT chk_tenant_subscription CHECK (subscription_plan IN ('FREE_TRIAL', 'BASIC', 'PROFESSIONAL', 'ENTERPRISE'))
);

CREATE INDEX idx_tenant_code ON tenants(code);
CREATE INDEX idx_tenant_slug ON tenants(slug);
CREATE INDEX idx_tenant_status ON tenants(status);
CREATE INDEX idx_tenant_email ON tenants(email);
