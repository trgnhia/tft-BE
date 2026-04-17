ALTER TABLE permissions
    ADD COLUMN IF NOT EXISTS created_by BIGINT,
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;

UPDATE permissions
SET created_by = 1
WHERE created_by IS NULL;

ALTER TABLE permissions
    ALTER COLUMN created_by SET NOT NULL;