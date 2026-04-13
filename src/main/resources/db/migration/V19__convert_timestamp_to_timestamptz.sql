BEGIN;

-- champ_item_recommend
ALTER TABLE champ_item_recommend
ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';

-- champs
ALTER TABLE champs
ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

-- cms_logs
ALTER TABLE cms_logs
ALTER COLUMN start_time TYPE timestamptz USING start_time AT TIME ZONE 'UTC',
    ALTER COLUMN end_time TYPE timestamptz USING end_time AT TIME ZONE 'UTC';

-- conversation_participants
ALTER TABLE conversation_participants
ALTER COLUMN joined_at TYPE timestamptz USING joined_at AT TIME ZONE 'UTC';

-- conversations
ALTER TABLE conversations
ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

-- items
ALTER TABLE items
ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

-- messages
ALTER TABLE messages
ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';

-- notifications
ALTER TABLE notifications
ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';

-- permissions
ALTER TABLE permissions
ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

-- role_permissions
ALTER TABLE role_permissions
ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';

-- roles
ALTER TABLE roles
ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

-- set
ALTER TABLE "set"
ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

-- team_comp
ALTER TABLE team_comp
ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

-- traits
ALTER TABLE traits
ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

-- users
ALTER TABLE users
ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC',
    ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC',
    ALTER COLUMN last_logout_at TYPE timestamptz USING last_logout_at AT TIME ZONE 'UTC';

COMMIT;