-- Users Table
CREATE TABLE users (
    id            BIGINT       NOT NULL PRIMARY KEY, -- Snowflake ID (User ID)
    email         VARCHAR(255) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    username      VARCHAR(50)  NOT NULL, -- 별명
    profile_image TEXT         NULL,
    created_at    TIMESTAMP    NULL
);

-- Servers Table
CREATE TABLE servers (
    id           BIGINT       NOT NULL PRIMARY KEY, -- Snowflake ID (Server ID)
    name         VARCHAR(100) NOT NULL,
    owner_id     BIGINT       NOT NULL, -- 채널(서버) 주인
    invite_code  VARCHAR(20)  NOT NULL,
    icon_image   TEXT         NULL,
    created_at   TIMESTAMP    NULL
);

-- Categories Table
CREATE TABLE categories (
    id            BIGINT       NOT NULL PRIMARY KEY, -- Snowflake ID
    server_id     BIGINT       NOT NULL,
    name          VARCHAR(100) NOT NULL,
    display_order INT          NULL,
    CONSTRAINT fk_categories_server FOREIGN KEY (server_id) REFERENCES servers (id)
);

-- Channels Table
CREATE TABLE channels (
    id            BIGINT       NOT NULL PRIMARY KEY, -- Snowflake ID
    category_id   BIGINT       NULL,
    server_id     BIGINT       NOT NULL,
    name          VARCHAR(100) NOT NULL,
    type          VARCHAR(20)  NULL DEFAULT 'TEXT', -- TEXT, VOICE
    display_order INT          NULL,
    CONSTRAINT fk_channels_server FOREIGN KEY (server_id) REFERENCES servers (id),
    CONSTRAINT fk_channels_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

-- Members Table (Server <-> User Join Table)
CREATE TABLE members (
    id        BIGSERIAL   NOT NULL PRIMARY KEY, -- 내부 관리용 Auto Increment
    server_id BIGINT      NOT NULL,
    user_id   BIGINT      NOT NULL,
    role      VARCHAR(20) NULL DEFAULT 'USER',
    joined_at TIMESTAMP   NULL,
    CONSTRAINT fk_members_server FOREIGN KEY (server_id) REFERENCES servers (id),
    CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users (id)
);
