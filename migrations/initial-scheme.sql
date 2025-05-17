--liquibase formatted sql

--changeset x-oc:1
CREATE TABLE IF NOT EXISTS chat
(
    id BIGINT NOT NULL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS link
(
    id               BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    url              TEXT UNIQUE,
    last_checked     TIMESTAMP WITH TIME ZONE,
    last_updated     TIMESTAMP WITH TIME ZONE,
    meta_information TEXT
);

CREATE TABLE IF NOT EXISTS chat_link
(
    chat_id BIGINT REFERENCES chat (id),
    link_id BIGINT REFERENCES link (id),
    PRIMARY KEY (chat_id, link_id)
);

CREATE TABLE IF NOT EXISTS tag
(
    id      BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    tag     TEXT,
    link_id BIGINT REFERENCES link (id)
);

CREATE TABLE IF NOT EXISTS filter
(
    id      BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    filter  TEXT,
    link_id BIGINT REFERENCES link (id)
);

--rollback drop table chat,link,chat_link;
