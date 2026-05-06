-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL    PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    user_name      VARCHAR(50)  NOT NULL,
    profile_image TEXT         NULL,
    created_at    TIMESTAMP    NULL,
    updated_at    TIMESTAMP    NULL
);

-- Servers Table (invite_code는 Redis에서 관리)
CREATE TABLE IF NOT EXISTS servers (
    id           BIGSERIAL    PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    owner_id     BIGINT       NOT NULL,
    icon_image   TEXT         NULL,
    created_at   TIMESTAMP    NULL,
    updated_at   TIMESTAMP    NULL
);

-- Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id            BIGSERIAL    PRIMARY KEY,
    server_id     BIGINT       NOT NULL,
    name          VARCHAR(100) NOT NULL,
    display_order INT          NULL,
    created_at    TIMESTAMP    NULL,
    updated_at    TIMESTAMP    NULL,
    CONSTRAINT fk_categories_server FOREIGN KEY (server_id) REFERENCES servers (id)
);

-- Channels Table
CREATE TABLE IF NOT EXISTS channels (
    id            BIGSERIAL    PRIMARY KEY,
    category_id   BIGINT       NULL,
    server_id     BIGINT       NULL,
    name          VARCHAR(100) NOT NULL,
    type          VARCHAR(20)  NULL DEFAULT 'TEXT',
    display_order INT          NULL,
    created_at    TIMESTAMP    NULL,
    updated_at    TIMESTAMP    NULL,
    CONSTRAINT fk_channels_server   FOREIGN KEY (server_id)   REFERENCES servers (id),
    CONSTRAINT fk_channels_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

-- Members Table
CREATE TABLE IF NOT EXISTS members (
    id         BIGSERIAL   NOT NULL PRIMARY KEY,
    server_id  BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    role       VARCHAR(20) NULL DEFAULT 'USER',
    joined_at  TIMESTAMP   NULL,
    created_at TIMESTAMP   NULL,
    updated_at TIMESTAMP   NULL,
    CONSTRAINT fk_members_server FOREIGN KEY (server_id) REFERENCES servers (id),
    CONSTRAINT fk_members_user   FOREIGN KEY (user_id)   REFERENCES users (id)
);

-- Chat Message Table
CREATE TABLE IF NOT EXISTS chat_message (
    id          BIGSERIAL    PRIMARY KEY,
    room_id     BIGINT       NOT NULL,
    sender_id   BIGINT       NOT NULL,
    sender_name VARCHAR(50)  NOT NULL,
    seq_id      BIGINT       NOT NULL,
    content     TEXT,
    created_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_chat_message_room_id   ON chat_message (room_id);
CREATE INDEX IF NOT EXISTS idx_chat_message_created_at ON chat_message (created_at);
CREATE INDEX IF NOT EXISTS idx_chat_message_room_seq  ON chat_message (room_id, seq_id);

-- DM Rooms Table
CREATE TABLE IF NOT EXISTS dm_rooms (
    id         BIGSERIAL PRIMARY KEY,
    user1_id   BIGINT NOT NULL,
    user2_id   BIGINT NOT NULL,
    channel_id BIGINT NOT NULL,
    CONSTRAINT unique_dm_room UNIQUE (user1_id, user2_id),
    CONSTRAINT fk_dm_channel FOREIGN KEY (channel_id) REFERENCES channels (id)
);

CREATE INDEX IF NOT EXISTS idx_dm_rooms_user1 ON dm_rooms (user1_id);
CREATE INDEX IF NOT EXISTS idx_dm_rooms_user2 ON dm_rooms (user2_id);

-- Friendships Table
CREATE TABLE IF NOT EXISTS friendships (
    id         BIGSERIAL   PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    friend_id  BIGINT      NOT NULL,
    status     VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'BLOCKED')),
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_friendship    UNIQUE (user_id, friend_id),
    CONSTRAINT fk_friendships_user   FOREIGN KEY (user_id)   REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_friend FOREIGN KEY (friend_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_status   ON friendships (user_id, status);
CREATE INDEX IF NOT EXISTS idx_friend_status ON friendships (friend_id, status);
