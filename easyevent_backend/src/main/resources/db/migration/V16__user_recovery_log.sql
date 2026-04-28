CREATE TABLE user_recovery_log (
    id BIGSERIAL PRIMARY KEY,
    target_user_id BIGINT NOT NULL,
    recovered_by_id BIGINT NOT NULL, -- User ID of the person who clicked 'Recover'
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Index for the 6-month cleanup task
CREATE INDEX idx_user_recovery_log_date ON user_recovery_log(occurred_at);
CREATE INDEX idx_user_recovery_log_target ON user_recovery_log(target_user_id);