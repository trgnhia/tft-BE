DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'notifications'
          AND column_name = 'message'
    ) THEN
ALTER TABLE notifications DROP COLUMN message;
END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'notifications'
          AND column_name = 'type'
    ) THEN
ALTER TABLE notifications ADD COLUMN type VARCHAR(50);
END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'notifications'
          AND column_name = 'type'
    ) THEN
ALTER TABLE notifications ALTER COLUMN type SET NOT NULL;
END IF;
END $$;