ALTER TABLE IF EXISTS "set"
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_name = 'set'
    )
    AND NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_set_updated_by'
    ) THEN
ALTER TABLE "set"
    ADD CONSTRAINT fk_set_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(id);
END IF;
END $$;