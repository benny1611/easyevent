-- Fast lookup by public token id + validity
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_valid
ON password_reset_tokens (id)
WHERE used = false;

CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_expires_at
ON password_reset_tokens (expires_at);