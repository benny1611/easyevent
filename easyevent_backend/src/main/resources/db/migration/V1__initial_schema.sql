-- USERS
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL
);

-- ROLES
CREATE TABLE roles (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

-- USER <-> ROLE
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- EVENTS
CREATE TABLE events (
    id BIGINT PRIMARY KEY,
    title TEXT NOT NULL,
    date TEXT NOT NULL,
    created_by BIGINT NOT NULL,
    FOREIGN KEY (created_by) REFERENCES users(id)
);
