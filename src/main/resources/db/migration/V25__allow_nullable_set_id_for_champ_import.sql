DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'champs'
          AND column_name = 'set_id'
          AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE champs
            ALTER COLUMN set_id DROP NOT NULL;
    END IF;
END $$;
