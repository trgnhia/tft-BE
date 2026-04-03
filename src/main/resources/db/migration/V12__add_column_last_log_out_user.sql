ALTER TABLE users
    ADD COLUMN IF NOT EXISTS last_logout_at timestamp NULL DEFAULT current_timestamp;