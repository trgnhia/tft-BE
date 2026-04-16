DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'set'
          AND column_name = 'description'
    ) THEN
ALTER TABLE "set"
    ADD COLUMN description TEXT;
END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'items'
          AND column_name = 'tier'
    ) THEN
ALTER TABLE items
    ADD COLUMN tier VARCHAR(10);
END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_items_tier'
    ) THEN
ALTER TABLE items
    ADD CONSTRAINT chk_items_tier
        CHECK (tier IS NULL OR tier IN ('S', 'A', 'B', 'C'));
END IF;
END $$;