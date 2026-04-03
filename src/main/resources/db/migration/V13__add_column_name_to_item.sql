DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'items'
          AND column_name = 'name'
    ) THEN
ALTER TABLE items
    ADD COLUMN name VARCHAR(100) NOT NULL DEFAULT '';
END IF;
END $$;