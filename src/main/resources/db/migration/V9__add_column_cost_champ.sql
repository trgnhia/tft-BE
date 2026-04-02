DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name='champs' AND column_name='cost') THEN
ALTER TABLE champs ADD COLUMN cost INTEGER NOT NULL DEFAULT 1;
END IF;
END $$;