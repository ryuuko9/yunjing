-- yunjing schema verification
-- Encoding: UTF-8

SET NAMES utf8mb4;
USE `yunjing`;

-- 1) table and column checks
SHOW TABLES LIKE 'project_publish_snapshot';

DESC `user`;
DESC `merchant_project`;
DESC `project_media_asset`;
DESC `project_model_asset`;
DESC `buyer_tutorial`;
DESC `project_publish_snapshot`;

-- 2) index checks
SHOW INDEX FROM `user`;
SHOW INDEX FROM `merchant_project`;
SHOW INDEX FROM `project_media_asset`;
SHOW INDEX FROM `project_model_asset`;
SHOW INDEX FROM `buyer_tutorial`;
SHOW INDEX FROM `project_publish_snapshot`;

-- 3) foreign key checks
SELECT
    tc.TABLE_NAME,
    tc.CONSTRAINT_NAME
FROM information_schema.TABLE_CONSTRAINTS tc
WHERE tc.TABLE_SCHEMA = DATABASE()
  AND tc.CONSTRAINT_TYPE = 'FOREIGN KEY'
  AND tc.TABLE_NAME IN (
      'merchant_project',
      'project_publish_snapshot',
      'project_media_asset',
      'project_model_asset',
      'buyer_tutorial'
  )
ORDER BY tc.TABLE_NAME, tc.CONSTRAINT_NAME;

-- 4) orphan checks
SELECT 'merchant_project.user_id orphan rows' AS check_name, COUNT(*) AS row_count
FROM `merchant_project` mp
LEFT JOIN `user` u ON u.`id` = mp.`user_id`
WHERE u.`id` IS NULL
UNION ALL
SELECT 'project_publish_snapshot.project_id orphan rows', COUNT(*)
FROM `project_publish_snapshot` pps
LEFT JOIN `merchant_project` mp ON mp.`id` = pps.`project_id`
WHERE mp.`id` IS NULL
UNION ALL
SELECT 'project_media_asset.project_id orphan rows', COUNT(*)
FROM `project_media_asset` pma
LEFT JOIN `merchant_project` mp ON mp.`id` = pma.`project_id`
WHERE mp.`id` IS NULL
UNION ALL
SELECT 'project_model_asset.project_id orphan rows', COUNT(*)
FROM `project_model_asset` pma
LEFT JOIN `merchant_project` mp ON mp.`id` = pma.`project_id`
WHERE mp.`id` IS NULL
UNION ALL
SELECT 'buyer_tutorial.buyer_user_id orphan rows', COUNT(*)
FROM `buyer_tutorial` bt
LEFT JOIN `user` u ON u.`id` = bt.`buyer_user_id`
WHERE u.`id` IS NULL
UNION ALL
SELECT 'buyer_tutorial.project_id orphan rows', COUNT(*)
FROM `buyer_tutorial` bt
LEFT JOIN `merchant_project` mp ON mp.`id` = bt.`project_id`
WHERE mp.`id` IS NULL
UNION ALL
SELECT 'buyer_tutorial.publish_snapshot_id orphan rows', COUNT(*)
FROM `buyer_tutorial` bt
LEFT JOIN `project_publish_snapshot` pps ON pps.`id` = bt.`publish_snapshot_id`
WHERE bt.`publish_snapshot_id` IS NOT NULL
  AND pps.`id` IS NULL;

-- 5) backfill checks
SELECT
    COUNT(*) AS snapshot_count,
    COUNT(DISTINCT `project_id`) AS distinct_project_count,
    COUNT(DISTINCT `publish_code`) AS distinct_publish_code_count
FROM `project_publish_snapshot`;

SELECT
    COUNT(*) AS buyer_tutorial_rows,
    SUM(CASE WHEN `publish_snapshot_id` IS NOT NULL THEN 1 ELSE 0 END) AS linked_snapshot_rows
FROM `buyer_tutorial`;

-- 6) explain checks for the optimized query paths
EXPLAIN SELECT *
FROM `merchant_project`
WHERE `user_id` = 1
  AND `is_deleted` = 0
ORDER BY `updated_at` DESC
LIMIT 20;

EXPLAIN SELECT *
FROM `project_media_asset`
WHERE `project_id` = 1
  AND `status` = 'ACTIVE'
ORDER BY `sort_order` ASC, `id` ASC;

EXPLAIN SELECT *
FROM `project_model_asset`
WHERE `project_id` = 1
  AND `status` = 'ACTIVE';

EXPLAIN SELECT *
FROM `project_publish_snapshot`
WHERE `publish_code` = 'demo_publish_code';

EXPLAIN SELECT *
FROM `buyer_tutorial`
WHERE `buyer_user_id` = 1
ORDER BY `updated_at` DESC
LIMIT 20;

-- 7) compatibility read check
SELECT
    mp.`id` AS project_id,
    COALESCE(pps.`publish_code`, mp.`publish_code`) AS resolved_publish_code,
    COALESCE(pps.`publish_url`, mp.`publish_url`) AS resolved_publish_url,
    COALESCE(pps.`tutorial_title`, mp.`tutorial_title`) AS resolved_tutorial_title,
    COALESCE(pps.`exploded_image_url`, mp.`exploded_image_url`) AS resolved_exploded_image_url,
    COALESCE(pps.`tutorial_video_url`, mp.`tutorial_video_url`) AS resolved_tutorial_video_url
FROM `merchant_project` mp
LEFT JOIN `project_publish_snapshot` pps
       ON pps.`project_id` = mp.`id`
ORDER BY mp.`id` DESC
LIMIT 20;

