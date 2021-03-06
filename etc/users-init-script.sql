SET LOCK_MODE 3;
SET CACHE_SIZE 33107;
;
CREATE USER IF NOT EXISTS SA PASSWORD '' ADMIN;
CREATE CACHED TABLE PUBLIC.USER(
    USERID VARCHAR(255) NOT NULL,
    ADMIN BOOLEAN,
    EMAIL VARCHAR(255),
    GIVENNAME VARCHAR(255),
    PASSWORD VARCHAR(255),
    ROLES VARCHAR(128000),
    SURNAME VARCHAR(255),
    TEAM_MANAGE BOOLEAN
);
ALTER TABLE PUBLIC.USER ADD CONSTRAINT PUBLIC.USER_PK PRIMARY KEY(USERID);
-- 2 +/- SELECT COUNT(*) FROM PUBLIC.USER;     
INSERT INTO PUBLIC.USER(USERID, ADMIN, EMAIL, GIVENNAME, PASSWORD, ROLES, SURNAME, TEAM_MANAGE) VALUES
('admin', TRUE, NULL, NULL, NULL, NULL, NULL, NULL),
('guest', FALSE, NULL, NULL, NULL, 'global.guest', NULL, FALSE);
CREATE INDEX PUBLIC.INDEX_GLOBAL_DATA_USER_SURNAME ON PUBLIC.USER(SURNAME);
CREATE INDEX PUBLIC.INDEX_GLOBAL_DATA_USER_GIVENNAME ON PUBLIC.USER(GIVENNAME);
CREATE INDEX PUBLIC.INDEX_GLOBAL_DATA_USER_ROLES ON PUBLIC.USER(ROLES);
CREATE INDEX PUBLIC.INDEX_GLOBAL_DATA_USER_ADMIN ON PUBLIC.USER(ADMIN);
CREATE INDEX PUBLIC.INDEX_GLOBAL_DATA_USER_TEAM_MANAGE ON PUBLIC.USER(TEAM_MANAGE);
CREATE INDEX PUBLIC.INDEX_GLOBAL_DATA_USER_EMAIL ON PUBLIC.USER(EMAIL);
CREATE INDEX PUBLIC.INDEX_GLOBAL_DATA_USER_PASSWORD ON PUBLIC.USER(PASSWORD);


