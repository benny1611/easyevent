-- expired tokens
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_expires_at
ON password_reset_tokens (expires_at);

-- already-used tokens
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_used_true
ON password_reset_tokens (id)
WHERE used = true;