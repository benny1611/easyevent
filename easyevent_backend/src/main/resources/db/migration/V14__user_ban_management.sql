-- 1. Create the Audit Table
CREATE TABLE user_ban_log (
    id BIGSERIAL PRIMARY KEY,
    target_user_id BIGINT NOT NULL,
    admin_id BIGINT NOT NULL,
    action_type TEXT NOT NULL CHECK (action_type IN ('BAN', 'UNBAN')),
    reason TEXT,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_log_target FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_log_admin FOREIGN KEY (admin_id) REFERENCES users(id)
);

-- 2. Create the Logic Function
CREATE OR REPLACE FUNCTION fn_sync_user_ban_state()
RETURNS TRIGGER AS $$
BEGIN
    -- Logic: If we log a BAN, set user state to 2 (BLOCKED)
    -- If we log an UNBAN, set user state to 1 (ACTIVE)
    IF NEW.action_type = 'BAN' THEN
        UPDATE users SET state_id = 2 WHERE id = NEW.target_user_id;
    ELSIF NEW.action_type = 'UNBAN' THEN
        UPDATE users SET state_id = 1 WHERE id = NEW.target_user_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 3. Bind the Trigger to the Table
CREATE TRIGGER trg_after_ban_log_insert
AFTER INSERT ON user_ban_log
FOR EACH ROW
EXECUTE FUNCTION fn_sync_user_ban_state();

-- 4. Index for fast audit lookups
CREATE INDEX idx_user_ban_log_target ON user_ban_log(target_user_id);