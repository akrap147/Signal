-- Users Table
CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    username      VARCHAR(50)  NOT NULL,
    profile_image TEXT         NULL,
    created_at    TIMESTAMP    NULL,
    updated_at    TIMESTAMP    NULL
);

-- Servers Table
CREATE TABLE servers (
    id           BIGSERIAL    PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    owner_id     BIGINT       NOT NULL,
    invite_code  VARCHAR(20)  NOT NULL,
    icon_image   TEXT         NULL,
    created_at   TIMESTAMP    NULL,
    updated_at   TIMESTAMP    NULL
);

-- Categories Table
CREATE TABLE categories (
    id            BIGSERIAL    PRIMARY KEY,
    server_id     BIGINT       NOT NULL,
    name          VARCHAR(100) NOT NULL,
    display_order INT          NULL,
    created_at    TIMESTAMP    NULL,
    updated_at    TIMESTAMP    NULL,
    CONSTRAINT fk_categories_server FOREIGN KEY (server_id) REFERENCES servers (id)
);

-- Channels Table
CREATE TABLE channels (
    id            BIGSERIAL    PRIMARY KEY,
    category_id   BIGINT       NULL,
    server_id     BIGINT       NOT NULL,
    name          VARCHAR(100) NOT NULL,
    type          VARCHAR(20)  NULL DEFAULT 'TEXT',
    display_order INT          NULL,
    created_at    TIMESTAMP    NULL,
    updated_at    TIMESTAMP    NULL,
    CONSTRAINT fk_channels_server FOREIGN KEY (server_id) REFERENCES servers (id),
    CONSTRAINT fk_channels_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

-- Members Table
CREATE TABLE members (
    id        BIGSERIAL   NOT NULL PRIMARY KEY,
    server_id BIGINT      NOT NULL,
    user_id   BIGINT      NOT NULL,
    role      VARCHAR(20) NULL DEFAULT 'USER',
    joined_at TIMESTAMP   NULL,
    created_at TIMESTAMP   NULL,
    updated_at TIMESTAMP   NULL,
    CONSTRAINT fk_members_server FOREIGN KEY (server_id) REFERENCES servers (id),
    CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users (id)
);
