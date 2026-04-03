
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_name = 'item_recommend'
    ) THEN
DROP TABLE item_recommend;
END IF;
END $$;

CREATE TABLE IF NOT EXISTS champ_item_recommend (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    champion_id BIGINT NOT NULL,
                                                    item_id BIGINT NOT NULL,
                                                    priority SMALLINT,
                                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                    created_by BIGINT,

                                                    CONSTRAINT fk_cir_champion
                                                    FOREIGN KEY (champion_id) REFERENCES champs(id),

    CONSTRAINT fk_cir_item
    FOREIGN KEY (item_id) REFERENCES items(id)
    );

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_cir_champion_item'
    ) THEN
ALTER TABLE champ_item_recommend
    ADD CONSTRAINT uq_cir_champion_item
        UNIQUE (champion_id, item_id);
END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_cir_champion_priority'
    ) THEN
ALTER TABLE champ_item_recommend
    ADD CONSTRAINT uq_cir_champion_priority
        UNIQUE (champion_id, priority);
END IF;
END $$;


CREATE INDEX IF NOT EXISTS idx_cir_champion_id
    ON champ_item_recommend(champion_id);

CREATE INDEX IF NOT EXISTS idx_cir_item_id
    ON champ_item_recommend(item_id);

ALTER TABLE items
DROP COLUMN IF EXISTS item_component_1,
DROP COLUMN IF EXISTS item_component_2;