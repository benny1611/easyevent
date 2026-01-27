-- GUESTS
CREATE TABLE guests (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE
);

-- EVENT REGISTRATIONS
CREATE TABLE event_registrations (
    id BIGSERIAL PRIMARY KEY,

    event_id BIGINT NOT NULL,
    user_id BIGINT,
    guest_id BIGINT,

    registered_at TIMESTAMPTZ NOT NULL,

    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE CASCADE,

    CHECK (
        (user_id IS NOT NULL AND guest_id IS NULL)
     OR (user_id IS NULL AND guest_id IS NOT NULL)
    )
);
