DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_name = 'champ_item_recommend'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'champ_item_recommend'
          AND column_name = 'deleted'
    ) THEN
ALTER TABLE champ_item_recommend
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;
END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_name = 'set'
    ) AND EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'set'
          AND column_name = 'is_active'
    ) THEN
ALTER TABLE "set"
DROP COLUMN is_active;
END IF;
END $$;