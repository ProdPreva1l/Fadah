# Set the storage engine
SET DEFAULT_STORAGE_ENGINE = INNODB;

# Enable foreign key constraints
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE IF NOT EXISTS collection_box (
    id INT AUTO_INCREMENT PRIMARY KEY,
    playerUUID VARCHAR(36) NOT NULL,
    itemStack TEXT NOT NULL,
    dateAdded BIGINT NOT NULL
) CHARACTER SET utf8
    COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS expired_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    playerUUID VARCHAR(36) NOT NULL,
    itemStack TEXT NOT NULL,
    dateAdded BIGINT NOT NULL
) CHARACTER SET utf8
    COLLATE utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS listings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL,
    ownerUUID VARCHAR(36) NOT NULL,
    ownerName VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    creationDate BIGINT NOT NULL,
    deletionDate BIGINT NOT NULL,
    price DOUBLE NOT NULL,
    itemStack TEXT NOT NULL
) CHARACTER SET utf8
    COLLATE utf8_unicode_ci;

-- Define the historic items table
CREATE TABLE IF NOT EXISTS history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    playerUUID VARCHAR(36) NOT NULL,
    loggedAction INT NOT NULL,
    loggedDate BIGINT NOT NULL,
    itemStack VARCHAR(255) NOT NULL,
    price DOUBLE NULLABLE,
    purchaserUUID VARCHAR(36) NULLABLE
) CHARACTER SET utf8
    COLLATE utf8_unicode_ci;