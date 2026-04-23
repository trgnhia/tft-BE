DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'set'
          AND column_name = 'code'
    ) THEN
        ALTER TABLE "set" ADD COLUMN code VARCHAR(100);
    END IF;
END $$;

UPDATE "set"
SET code = btrim(code)
WHERE code IS NOT NULL;

UPDATE "set" s
SET code = CASE
    WHEN btrim(regexp_replace(lower(coalesce(s.name, '')), '[^a-z0-9]+', '-', 'g'), '-') <> ''
        THEN left(btrim(regexp_replace(lower(coalesce(s.name, '')), '[^a-z0-9]+', '-', 'g'), '-'), 90)
    ELSE 'set-' || s.id::text
END
WHERE s.code IS NULL OR btrim(s.code) = '';

UPDATE "set" s
SET code = left(s.code, 79) || '-' || s.id::text
WHERE EXISTS (
    SELECT 1
    FROM "set" x
    WHERE lower(x.code) = lower(s.code)
      AND x.id < s.id
);

UPDATE "set" s
SET code = 'set-' || s.id::text
WHERE s.code IS NULL OR btrim(s.code) = '';

ALTER TABLE "set"
    ALTER COLUMN code SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_set_code_lower
    ON "set" (lower(code));
