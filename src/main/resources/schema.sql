-- Runs on every startup against MySQL. IF NOT EXISTS keeps the rows already stored in the database volume.
CREATE TABLE IF NOT EXISTS names (
    id   BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);
