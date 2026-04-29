# yunjing 数据库迁移说明

## 文件
- `database/migrations/2026-04-27_yunjing_incremental_schema.sql`
- `database/verification/2026-04-27_yunjing_schema_verification.sql`

## 执行顺序
1. 在 MySQL Workbench 中连接目标库。
2. 执行 `SET NAMES utf8mb4;`，确保会话使用 UTF-8。
3. 执行迁移脚本 `database/migrations/2026-04-27_yunjing_incremental_schema.sql`。
4. 执行验证脚本 `database/verification/2026-04-27_yunjing_schema_verification.sql`。

## 说明
- 迁移脚本设计为可重复执行：新增列、索引、外键都带存在性检查。
- 外键在发现孤儿数据时会跳过，并返回 `migration_warning`，不会强行删除历史数据。
- `project_publish_snapshot` 会从 `merchant_project` 的现有发布字段回填，并把 `buyer_tutorial.publish_snapshot_id` 自动关联上。
- 兼容期内，`merchant_project` 原有发布字段不会删除，现有 Android DTO 可继续使用。
