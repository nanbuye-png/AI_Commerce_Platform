-- ============================================================
-- V1: Initial Schema
-- Description: Create initial tables for AI Commerce Platform
-- This migration matches the JPA entities for production safety
-- ============================================================

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(100),
    avatar VARCHAR(500),
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_time TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE
);

-- Roles table (RBAC)
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_time TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE
);

-- Permissions table
CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_time TIMESTAMP WITHOUT TIME ZONE,
    updated_time TIMESTAMP WITHOUT TIME ZONE
);

-- Role-Permission mapping
CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    created_time TIMESTAMP WITHOUT TIME ZONE,
    UNIQUE(role_id, permission_id)
);

-- User-Role mapping (supports multiple roles per user)
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    created_time TIMESTAMP WITHOUT TIME ZONE,
    UNIQUE(user_id, role_id)
);

-- Seed data: Default roles
INSERT INTO roles (name, description, created_time, updated_time)
VALUES
    ('CUSTOMER', 'Regular customer who browses and purchases products', NOW(), NOW()),
    ('MERCHANT', 'Shop owner who manages products and orders', NOW(), NOW()),
    ('ADMIN', 'Platform administrator with full access', NOW(), NOW()),
    ('SUPER_ADMIN', 'Super administrator with system-level access', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Seed data: Default permissions
INSERT INTO permissions (code, name, description, created_time, updated_time)
VALUES
    ('customer:read', 'Customer Read', 'Access customer-facing features', NOW(), NOW()),
    ('customer:write', 'Customer Write', 'Create and modify own cart, orders, profile', NOW(), NOW()),
    ('merchant:read', 'Merchant Read', 'Access merchant dashboard', NOW(), NOW()),
    ('merchant:write', 'Merchant Write', 'Manage products, view orders', NOW(), NOW()),
    ('admin:read', 'Admin Read', 'Access admin console', NOW(), NOW()),
    ('admin:write', 'Admin Write', 'Manage users, merchants, system config', NOW(), NOW()),
    ('system:config', 'System Config', 'Modify system-level configuration', NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- Seed data: Role-Permission mappings
INSERT INTO role_permissions (role_id, permission_id, created_time)
SELECT r.id, p.id, NOW()
FROM roles r, permissions p
WHERE
    (r.name = 'CUSTOMER' AND p.code IN ('customer:read', 'customer:write')) OR
    (r.name = 'MERCHANT' AND p.code IN ('merchant:read', 'merchant:write', 'customer:read', 'customer:write')) OR
    (r.name = 'ADMIN' AND p.code IN ('admin:read', 'admin:write', 'merchant:read', 'customer:read')) OR
    (r.name = 'SUPER_ADMIN' AND p.code IN ('admin:read', 'admin:write', 'system:config', 'merchant:read', 'merchant:write', 'customer:read', 'customer:write'))
ON CONFLICT (role_id, permission_id) DO NOTHING;