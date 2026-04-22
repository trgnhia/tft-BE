DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'champs'
          AND column_name = 'code'
    ) THEN
        ALTER TABLE champs ADD COLUMN code VARCHAR(100);
    END IF;
END $$;

UPDATE champs
SET code = slug
WHERE code IS NULL OR btrim(code) = '';

ALTER TABLE champs
    ALTER COLUMN code SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_champs_code_lower
    ON champs (lower(code));
