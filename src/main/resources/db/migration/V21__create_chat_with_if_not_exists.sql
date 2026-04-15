CREATE TABLE IF NOT EXISTS conversations (
                                             id BIGSERIAL PRIMARY KEY,
                                             created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_conversations_updated_at
    ON conversations(updated_at DESC);


CREATE TABLE IF NOT EXISTS conversation_participants (
                                                         id BIGSERIAL PRIMARY KEY,
                                                         conversation_id BIGINT NOT NULL,
                                                         user_id BIGINT NOT NULL,
                                                         joined_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_cp_conversation
    FOREIGN KEY (conversation_id)
    REFERENCES conversations(id)
    ON DELETE CASCADE,

    CONSTRAINT fk_cp_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE,

    CONSTRAINT uq_cp_conversation_user
    UNIQUE (conversation_id, user_id)
    );

CREATE INDEX IF NOT EXISTS idx_cp_user_id
    ON conversation_participants(user_id);

CREATE INDEX IF NOT EXISTS idx_cp_conversation_id
    ON conversation_participants(conversation_id);


CREATE TABLE IF NOT EXISTS messages (
                                        id BIGSERIAL PRIMARY KEY,
                                        conversation_id BIGINT NOT NULL,
                                        sender_id BIGINT NOT NULL,
                                        content TEXT NOT NULL,
                                        created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_messages_conversation
    FOREIGN KEY (conversation_id)
    REFERENCES conversations(id)
    ON DELETE CASCADE,

    CONSTRAINT fk_messages_sender
    FOREIGN KEY (sender_id)
    REFERENCES users(id)
    ON DELETE CASCADE,

    CONSTRAINT chk_messages_content_not_blank
    CHECK (length(trim(content)) > 0)
    );

CREATE INDEX IF NOT EXISTS idx_messages_conversation_created
    ON messages(conversation_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_messages_sender
    ON messages(sender_id);