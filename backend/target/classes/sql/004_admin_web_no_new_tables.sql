USE hospital_service;

-- 管理端 Web（Vue + Element Plus）仅使用既有业务表，无需新建表。
-- 依赖：已执行 001_init_schema.sql、002_doctor_patient_bind.sql、003_patient_file_asset.sql
-- 管理员账号：sys_user.role = 1，由应用启动脚本或手工插入。

-- 以下为可选索引（大数据量时提升后台列表查询；无则跳过本段）
-- CREATE INDEX idx_sys_user_role_deleted ON sys_user (role, deleted, status);
