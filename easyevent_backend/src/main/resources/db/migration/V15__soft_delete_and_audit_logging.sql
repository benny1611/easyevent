-- 1. Add soft-delete column to users
ALTER TABLE users ADD COLUMN deleted_at TIMESTAMPTZ DEFAULT NULL;

-- 2. Fix Email Uniqueness: Only unique for ACTIVE users
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_email_key;
CREATE UNIQUE INDEX idx_users_email_active ON users(email) WHERE (deleted_at IS NULL);

-- 3. Create the Audit Log table
CREATE TABLE user_deletion_log (
    id BIGSERIAL PRIMARY KEY,
    target_user_id BIGINT NOT NULL, -- The user being deleted
    actor_id BIGINT NOT NULL,      -- The admin OR the user themselves
    deletion_type TEXT NOT NULL CHECK (deletion_type IN ('SELF', 'ADMIN')),
    reason TEXT,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Index for the cleanup tasks
CREATE INDEX idx_users_deleted_at ON users(deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX idx_deletion_log_date ON user_deletion_log(occurred_at);