-- liquibase formatted sql

-- changeset x-oc:1
-- precondition: tableExists tag
-- precondition: tableExists link
-- precondition: tableExists chat_link
BEGIN;

CREATE TABLE IF NOT EXISTS new_tag (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name TEXT NOT NULL,
    chat_id BIGINT NOT NULL REFERENCES chat(id),
    UNIQUE (name, chat_id)
);

CREATE TABLE IF NOT EXISTS link_tag (
    link_id BIGINT NOT NULL REFERENCES link(id),
    tag_id BIGINT NOT NULL REFERENCES new_tag(id),
    PRIMARY KEY (link_id, tag_id)
);

INSERT INTO new_tag (name, chat_id)
SELECT DISTINCT t.tag, cl.chat_id
FROM tag t
    JOIN link l ON t.link_id = l.id
    JOIN chat_link cl ON cl.link_id = l.id;

INSERT INTO link_tag (link_id, tag_id)
SELECT t.link_id, nt.id
FROM tag t
    JOIN new_tag nt ON t.tag = nt.name
    JOIN chat_link cl ON cl.chat_id = nt.chat_id AND cl.link_id = t.link_id;

DROP TABLE tag;

ALTER TABLE new_tag RENAME TO tag;

COMMIT;
