CREATE TABLE chat_message (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    seq_id BIGINT NOT NULL,
    content TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chat_message_room_id ON chat_message (room_id);
CREATE INDEX idx_chat_message_created_at ON chat_message (created_at);
CREATE INDEX idx_chat_message_room_seq ON chat_message (room_id, seq_id);
