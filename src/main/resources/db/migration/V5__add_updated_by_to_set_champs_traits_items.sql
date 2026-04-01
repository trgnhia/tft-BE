
ALTER TABLE IF EXISTS sets
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;

ALTER TABLE IF EXISTS champs
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;

ALTER TABLE IF EXISTS traits
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;

ALTER TABLE IF EXISTS items
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;


DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_name = 'sets'
    ) AND NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_sets_updated_by'
    ) THEN
ALTER TABLE sets
    ADD CONSTRAINT fk_sets_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(id);
END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_name = 'champs'
    ) AND NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_champs_updated_by'
    ) THEN
ALTER TABLE champs
    ADD CONSTRAINT fk_champs_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(id);
END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_name = 'traits'
    ) AND NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_traits_updated_by'
    ) THEN
ALTER TABLE traits
    ADD CONSTRAINT fk_traits_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(id);
END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_name = 'items'
    ) AND NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_items_updated_by'
    ) THEN
ALTER TABLE items
    ADD CONSTRAINT fk_items_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(id);
END IF;
END $$;

