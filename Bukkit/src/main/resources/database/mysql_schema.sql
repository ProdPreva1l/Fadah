CREATE TABLE `items` (
    `id`         INT AUTO_INCREMENT NOT NULL,
    `uuid`       VARCHAR(36)        NOT NULL UNIQUE,
    `owner_id`   VARCHAR(36)        NOT NULL,
    `owner_name` VARCHAR(255),
    `buyer_id`   VARCHAR(36),
    `item`       TEXT               NOT NULL,
    `category`   VARCHAR(255),
    `price`      DOUBLE,
    `tax`        DOUBLE,
    `time`       BIGINT             NOT NULL,
    `biddable`   BOOLEAN,
    `bids`       TEXT,
    `update`     BIGINT,
    `collected`  BOOLEAN,
    PRIMARY KEY (`id`)
) CHARACTER SET utf8
  COLLATE utf8_unicode_ci;