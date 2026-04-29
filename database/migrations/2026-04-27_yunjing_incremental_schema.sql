-- yunjing incremental schema optimization
-- Encoding: UTF-8
-- Safe to re-run on the same schema.

SET NAMES utf8mb4;
USE `yunjing`;

DROP PROCEDURE IF EXISTS `sp_ensure_column`;
DROP PROCEDURE IF EXISTS `sp_ensure_index`;
DROP PROCEDURE IF EXISTS `sp_ensure_fk_if_clean`;

DELIMITER $$

CREATE PROCEDURE `sp_ensure_column`(
    IN p_schema_name VARCHAR(64),
    IN p_table_name VARCHAR(64),
    IN p_column_name VARCHAR(64),
    IN p_column_definition TEXT
)
BEGIN
    DECLARE v_exists INT DEFAULT 0;

    SELECT COUNT(*)
    INTO v_exists
    FROM information_schema.columns
    WHERE table_schema = p_schema_name
      AND table_name = p_table_name
      AND column_name = p_column_name;

    IF v_exists = 0 THEN
        SET @ddl_sql = CONCAT(
            'ALTER TABLE `', p_schema_name, '`.`', p_table_name,
            '` ADD COLUMN `', p_column_name, '` ', p_column_definition
        );
        PREPARE ddl_stmt FROM @ddl_sql;
        EXECUTE ddl_stmt;
        DEALLOCATE PREPARE ddl_stmt;
    END IF;
END$$

CREATE PROCEDURE `sp_ensure_index`(
    IN p_schema_name VARCHAR(64),
    IN p_table_name VARCHAR(64),
    IN p_index_name VARCHAR(64),
    IN p_index_sql TEXT
)
BEGIN
    DECLARE v_exists INT DEFAULT 0;

    SELECT COUNT(*)
    INTO v_exists
    FROM information_schema.statistics
    WHERE table_schema = p_schema_name
      AND table_name = p_table_name
      AND index_name = p_index_name;

    IF v_exists = 0 THEN
        SET @ddl_sql = p_index_sql;
        PREPARE ddl_stmt FROM @ddl_sql;
        EXECUTE ddl_stmt;
        DEALLOCATE PREPARE ddl_stmt;
    END IF;
END$$

CREATE PROCEDURE `sp_ensure_fk_if_clean`(
    IN p_schema_name VARCHAR(64),
    IN p_table_name VARCHAR(64),
    IN p_fk_name VARCHAR(64),
    IN p_orphan_query TEXT,
    IN p_fk_sql TEXT
)
BEGIN
    DECLARE v_exists INT DEFAULT 0;
    DECLARE v_orphans BIGINT DEFAULT 0;

    SELECT COUNT(*)
    INTO v_exists
    FROM information_schema.referential_constraints
    WHERE constraint_schema = p_schema_name
      AND table_name = p_table_name
      AND constraint_name = p_fk_name;

    IF v_exists = 0 THEN
        SET @orphan_sql = CONCAT(
            'SELECT COUNT(*) INTO @fk_orphan_count FROM (',
            p_orphan_query,
            ') orphan_rows'
        );
        PREPARE orphan_stmt FROM @orphan_sql;
        EXECUTE orphan_stmt;
        DEALLOCATE PREPARE orphan_stmt;

        SET v_orphans = COALESCE(@fk_orphan_count, 0);

        IF v_orphans = 0 THEN
            SET @ddl_sql = p_fk_sql;
            PREPARE ddl_stmt FROM @ddl_sql;
            EXECUTE ddl_stmt;
            DEALLOCATE PREPARE ddl_stmt;
        ELSE
            SELECT CONCAT(
                'SKIPPED foreign key ',
                p_fk_name,
                ' because orphan rows = ',
                v_orphans
            ) AS migration_warning;
        END IF;
    END IF;
END$$

DELIMITER ;

-- user
CALL `sp_ensure_column`(
    DATABASE(),
    'user',
    'password_hash',
    'VARCHAR(255) NULL COMMENT ''password hash for phased migration'''
);

CALL `sp_ensure_index`(
    DATABASE(),
    'user',
    'idx_user_role_status',
    'CREATE INDEX `idx_user_role_status` ON `user` (`role`, `status`)'
);

-- merchant_project compatibility columns
CALL `sp_ensure_column`(DATABASE(), 'merchant_project', 'parse_mode', 'VARCHAR(50) NULL');
CALL `sp_ensure_column`(DATABASE(), 'merchant_project', 'parse_result_text', 'VARCHAR(500) NULL');
CALL `sp_ensure_column`(DATABASE(), 'merchant_project', 'exploded_image_url', 'VARCHAR(500) NULL');
CALL `sp_ensure_column`(DATABASE(), 'merchant_project', 'tutorial_video_url', 'VARCHAR(500) NULL');
CALL `sp_ensure_column`(DATABASE(), 'merchant_project', 'tutorial_title', 'VARCHAR(200) NULL');
CALL `sp_ensure_column`(DATABASE(), 'merchant_project', 'publish_code', 'VARCHAR(64) NULL');
CALL `sp_ensure_column`(DATABASE(), 'merchant_project', 'publish_url', 'VARCHAR(500) NULL');
CALL `sp_ensure_column`(DATABASE(), 'merchant_project', 'qr_code_base64', 'LONGTEXT NULL');
CALL `sp_ensure_column`(DATABASE(), 'merchant_project', 'published_at', 'DATETIME NULL');

ALTER TABLE `merchant_project`
    MODIFY COLUMN `cover_url` VARCHAR(500) NULL,
    MODIFY COLUMN `publish_url` VARCHAR(500) NULL;

CALL `sp_ensure_index`(
    DATABASE(),
    'merchant_project',
    'idx_project_user_deleted_updated',
    'CREATE INDEX `idx_project_user_deleted_updated` ON `merchant_project` (`user_id`, `is_deleted`, `updated_at`)'
);

CALL `sp_ensure_index`(
    DATABASE(),
    'merchant_project',
    'idx_project_publish_code',
    'CREATE INDEX `idx_project_publish_code` ON `merchant_project` (`publish_code`)'
);

CALL `sp_ensure_index`(
    DATABASE(),
    'merchant_project',
    'idx_project_status_combo',
    'CREATE INDEX `idx_project_status_combo` ON `merchant_project` (`publish_status`, `parse_status`, `rebuild_status`)'
);

-- media/model compatibility columns
CALL `sp_ensure_column`(
    DATABASE(),
    'project_media_asset',
    'file_path',
    'VARCHAR(500) NULL COMMENT ''local or object storage path'''
);

CALL `sp_ensure_column`(
    DATABASE(),
    'project_model_asset',
    'file_path',
    'VARCHAR(500) NULL COMMENT ''local or object storage path'''
);

-- publish snapshot table
CREATE TABLE IF NOT EXISTS `project_publish_snapshot` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `project_id` BIGINT NOT NULL,
    `publish_code` VARCHAR(64) NOT NULL,
    `publish_url` VARCHAR(500) NULL,
    `qr_code_base64` LONGTEXT NULL,
    `tutorial_title` VARCHAR(200) NULL,
    `project_desc` VARCHAR(500) NULL,
    `cover_url` VARCHAR(500) NULL,
    `exploded_image_url` VARCHAR(500) NULL,
    `tutorial_video_url` VARCHAR(500) NULL,
    `published_at` DATETIME NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_publish_snapshot_code` (`publish_code`),
    UNIQUE KEY `uk_publish_snapshot_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CALL `sp_ensure_column`(
    DATABASE(),
    'buyer_tutorial',
    'publish_snapshot_id',
    'BIGINT NULL COMMENT ''linked publish snapshot id'''
);

-- backfill publish snapshot from merchant_project
INSERT INTO `project_publish_snapshot` (
    `project_id`,
    `publish_code`,
    `publish_url`,
    `qr_code_base64`,
    `tutorial_title`,
    `project_desc`,
    `cover_url`,
    `exploded_image_url`,
    `tutorial_video_url`,
    `published_at`,
    `status`
)
SELECT
    mp.`id`,
    mp.`publish_code`,
    mp.`publish_url`,
    mp.`qr_code_base64`,
    mp.`tutorial_title`,
    mp.`project_desc`,
    mp.`cover_url`,
    mp.`exploded_image_url`,
    mp.`tutorial_video_url`,
    mp.`published_at`,
    CASE
        WHEN mp.`publish_status` IS NULL OR TRIM(mp.`publish_status`) = '' THEN 'PUBLISHED'
        ELSE mp.`publish_status`
    END
FROM `merchant_project` mp
WHERE mp.`publish_code` IS NOT NULL
  AND TRIM(mp.`publish_code`) <> ''
ON DUPLICATE KEY UPDATE
    `publish_url` = VALUES(`publish_url`),
    `qr_code_base64` = VALUES(`qr_code_base64`),
    `tutorial_title` = VALUES(`tutorial_title`),
    `project_desc` = VALUES(`project_desc`),
    `cover_url` = VALUES(`cover_url`),
    `exploded_image_url` = VALUES(`exploded_image_url`),
    `tutorial_video_url` = VALUES(`tutorial_video_url`),
    `published_at` = VALUES(`published_at`),
    `status` = VALUES(`status`),
    `updated_at` = CURRENT_TIMESTAMP;

UPDATE `buyer_tutorial` bt
INNER JOIN `project_publish_snapshot` pps
        ON pps.`project_id` = bt.`project_id`
       AND pps.`publish_code` = bt.`publish_code`
SET bt.`publish_snapshot_id` = pps.`id`
WHERE bt.`publish_snapshot_id` IS NULL
   OR bt.`publish_snapshot_id` <> pps.`id`;

-- child table indexes
CALL `sp_ensure_index`(
    DATABASE(),
    'project_media_asset',
    'idx_media_project_status_sort',
    'CREATE INDEX `idx_media_project_status_sort` ON `project_media_asset` (`project_id`, `status`, `sort_order`)'
);

CALL `sp_ensure_index`(
    DATABASE(),
    'project_media_asset',
    'idx_media_project_type',
    'CREATE INDEX `idx_media_project_type` ON `project_media_asset` (`project_id`, `asset_type`)'
);

CALL `sp_ensure_index`(
    DATABASE(),
    'project_model_asset',
    'idx_model_project_status',
    'CREATE INDEX `idx_model_project_status` ON `project_model_asset` (`project_id`, `status`)'
);

CALL `sp_ensure_index`(
    DATABASE(),
    'project_model_asset',
    'idx_model_project_source_visible',
    'CREATE INDEX `idx_model_project_source_visible` ON `project_model_asset` (`project_id`, `source_type`, `visible_after_rebuild`)'
);

CALL `sp_ensure_index`(
    DATABASE(),
    'buyer_tutorial',
    'idx_buyer_tutorial_user_updated',
    'CREATE INDEX `idx_buyer_tutorial_user_updated` ON `buyer_tutorial` (`buyer_user_id`, `updated_at`)'
);

CALL `sp_ensure_index`(
    DATABASE(),
    'buyer_tutorial',
    'idx_buyer_tutorial_publish_code',
    'CREATE INDEX `idx_buyer_tutorial_publish_code` ON `buyer_tutorial` (`publish_code`)'
);

CALL `sp_ensure_index`(
    DATABASE(),
    'buyer_tutorial',
    'idx_buyer_tutorial_publish_snapshot',
    'CREATE INDEX `idx_buyer_tutorial_publish_snapshot` ON `buyer_tutorial` (`publish_snapshot_id`)'
);

-- foreign keys are added only when orphan data is clean
CALL `sp_ensure_fk_if_clean`(
    DATABASE(),
    'merchant_project',
    'fk_merchant_project_user',
    'SELECT mp.id FROM `merchant_project` mp LEFT JOIN `user` u ON u.id = mp.user_id WHERE u.id IS NULL',
    'ALTER TABLE `merchant_project` ADD CONSTRAINT `fk_merchant_project_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON UPDATE CASCADE ON DELETE RESTRICT'
);

CALL `sp_ensure_fk_if_clean`(
    DATABASE(),
    'project_publish_snapshot',
    'fk_publish_snapshot_project',
    'SELECT pps.id FROM `project_publish_snapshot` pps LEFT JOIN `merchant_project` mp ON mp.id = pps.project_id WHERE mp.id IS NULL',
    'ALTER TABLE `project_publish_snapshot` ADD CONSTRAINT `fk_publish_snapshot_project` FOREIGN KEY (`project_id`) REFERENCES `merchant_project` (`id`) ON UPDATE CASCADE ON DELETE RESTRICT'
);

CALL `sp_ensure_fk_if_clean`(
    DATABASE(),
    'project_media_asset',
    'fk_media_asset_project',
    'SELECT pma.id FROM `project_media_asset` pma LEFT JOIN `merchant_project` mp ON mp.id = pma.project_id WHERE mp.id IS NULL',
    'ALTER TABLE `project_media_asset` ADD CONSTRAINT `fk_media_asset_project` FOREIGN KEY (`project_id`) REFERENCES `merchant_project` (`id`) ON UPDATE CASCADE ON DELETE RESTRICT'
);

CALL `sp_ensure_fk_if_clean`(
    DATABASE(),
    'project_model_asset',
    'fk_model_asset_project',
    'SELECT pma.id FROM `project_model_asset` pma LEFT JOIN `merchant_project` mp ON mp.id = pma.project_id WHERE mp.id IS NULL',
    'ALTER TABLE `project_model_asset` ADD CONSTRAINT `fk_model_asset_project` FOREIGN KEY (`project_id`) REFERENCES `merchant_project` (`id`) ON UPDATE CASCADE ON DELETE RESTRICT'
);

CALL `sp_ensure_fk_if_clean`(
    DATABASE(),
    'buyer_tutorial',
    'fk_buyer_tutorial_user',
    'SELECT bt.id FROM `buyer_tutorial` bt LEFT JOIN `user` u ON u.id = bt.buyer_user_id WHERE u.id IS NULL',
    'ALTER TABLE `buyer_tutorial` ADD CONSTRAINT `fk_buyer_tutorial_user` FOREIGN KEY (`buyer_user_id`) REFERENCES `user` (`id`) ON UPDATE CASCADE ON DELETE RESTRICT'
);

CALL `sp_ensure_fk_if_clean`(
    DATABASE(),
    'buyer_tutorial',
    'fk_buyer_tutorial_project',
    'SELECT bt.id FROM `buyer_tutorial` bt LEFT JOIN `merchant_project` mp ON mp.id = bt.project_id WHERE mp.id IS NULL',
    'ALTER TABLE `buyer_tutorial` ADD CONSTRAINT `fk_buyer_tutorial_project` FOREIGN KEY (`project_id`) REFERENCES `merchant_project` (`id`) ON UPDATE CASCADE ON DELETE RESTRICT'
);

CALL `sp_ensure_fk_if_clean`(
    DATABASE(),
    'buyer_tutorial',
    'fk_buyer_tutorial_publish_snapshot',
    'SELECT bt.id FROM `buyer_tutorial` bt LEFT JOIN `project_publish_snapshot` pps ON pps.id = bt.publish_snapshot_id WHERE bt.publish_snapshot_id IS NOT NULL AND pps.id IS NULL',
    'ALTER TABLE `buyer_tutorial` ADD CONSTRAINT `fk_buyer_tutorial_publish_snapshot` FOREIGN KEY (`publish_snapshot_id`) REFERENCES `project_publish_snapshot` (`id`) ON UPDATE CASCADE ON DELETE RESTRICT'
);

DROP PROCEDURE IF EXISTS `sp_ensure_column`;
DROP PROCEDURE IF EXISTS `sp_ensure_index`;
DROP PROCEDURE IF EXISTS `sp_ensure_fk_if_clean`;

