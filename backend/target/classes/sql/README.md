# 数据库脚本执行说明

## 执行顺序（必须按序号从小到大）

| 顺序 | 文件名 | 说明 |
| --- | --- | --- |
| 1 | [`001_init_schema.sql`](001_init_schema.sql) | 创建库 `hospital_service`；表 `sys_user`、`doctor_profile`、`patient_profile` |
| 2 | [`002_doctor_patient_bind.sql`](002_doctor_patient_bind.sql) | 表 `doctor_patient_bind`（医患绑定申请与审核，依赖 `sys_user` / 医生患者账号） |
| 3 | [`003_patient_file_asset.sql`](003_patient_file_asset.sql) | 表 `patient_file_asset`（患者附件元数据；文件本体存服务器本地目录） |
| 4 | [`004_admin_web_no_new_tables.sql`](004_admin_web_no_new_tables.sql) | 管理端 Web 说明与可选索引（**不新增表**；依赖 001～003） |

> **新环境**：按上表从 `1` 起依次执行即可。  
> **已有环境**：只执行尚未跑过的、序号更大的新脚本（见下节「增量变更」）。

## 增量变更（以后新增表/字段时）

1. **不要**再改已发布过的基线脚本内容（避免与已部署库不一致）。
2. 新增独立文件，命名：`三位序号_简短描述.sql`  
   - 示例：`002_doctor_patient_bind.sql`、`003_notice.sql`
3. 在本文件表格中**追加一行**，写明顺序、文件名、说明。
4. 脚本内建议：使用 `USE hospital_service;`，对表结构用 `CREATE TABLE IF NOT EXISTS` 或显式 `ALTER TABLE`，并在注释中写清依赖（依赖哪些已有表）。

## 历史说明

- 原 `init_schema.sql` 已合并为 **`001_init_schema.sql`**，请以后以本目录 `README.md` 为准执行。
