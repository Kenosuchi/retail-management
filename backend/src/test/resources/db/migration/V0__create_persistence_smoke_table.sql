-- Test fixture only: production migrations begin at V1.
CREATE TABLE persistence_smoke (
    id BIGINT NOT NULL PRIMARY KEY,
    message VARCHAR(100) NOT NULL
);
