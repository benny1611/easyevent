-- USER STATES
CREATE TABLE user_states (
    id SMALLSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);


INSERT INTO user_states (id, name) VALUES
    (1, 'ACTIVE'),
    (2, 'BLOCKED');

-- OAUTH PROVIDERS
CREATE TABLE oauth_providers (
    id SMALLSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

INSERT INTO oauth_providers (id, name) VALUES
    (1, 'GOOGLE'),
    (2, 'MICROSOFT');

-- EXTEND USERS TABLE
ALTER TABLE users
    -- allow OAuth-only users
    ALTER COLUMN password DROP NOT NULL,

    -- last successful login (password or OAuth)
    ADD COLUMN last_login_at TIMESTAMPTZ,

    -- brute-force protection
    ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0,

    -- user state
    ADD COLUMN state_id SMALLINT NOT NULL DEFAULT 1;


ALTER TABLE users
ADD CONSTRAINT fk_users_state
FOREIGN KEY (state_id) REFERENCES user_states(id);

-- USER <-> OAUTH ACCOUNTS
CREATE TABLE user_oauth_accounts (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    provider_id SMALLINT NOT NULL,

    -- OAuth / OpenID Connect "sub"
    provider_user_id TEXT NOT NULL,

    email TEXT,
    connected_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (provider_id) REFERENCES oauth_providers(id) ON DELETE CASCADE,

    -- one OAuth account = one user
    UNIQUE (provider_id, provider_user_id),

    -- one provider per user
    UNIQUE (user_id, provider_id)
);

-- INDEXES (LOGIN PERFORMANCE)
CREATE INDEX idx_users_state_id
    ON users(state_id);

CREATE INDEX idx_user_oauth_accounts_user_id
    ON user_oauth_accounts(user_id);

CREATE INDEX idx_user_oauth_accounts_provider_lookup
    ON user_oauth_accounts(provider_id, provider_user_id);