ALTER TABLE users
ADD COLUMN activation_token UUID UNIQUE;

ALTER TABLE users
ADD COLUMN activation_sent_at TIMESTAMPTZ;
