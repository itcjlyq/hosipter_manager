USE hospital_service;

-- 新增或修复系统管理员账号（可重复执行）
-- 默认账号：13800000000
-- 默认密码：admin123456
-- 密码哈希算法：BCrypt（与后端 PasswordEncoder 一致）
INSERT INTO sys_user (
    phone,
    password_hash,
    role,
    status,
    deleted,
    created_at,
    updated_at
) VALUES (
    '13800000000',
    '$2b$10$pWvE6JvcQAnmRWGWg.oLIuAS0lTQqGhNBQQWLCjP9vQkVFfz0.PxO',
    1,
    1,
    0,
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    role = 1,
    status = 1,
    deleted = 0,
    updated_at = NOW();

-- 新增或修复医生测试账号：13800000001 / 123456
INSERT INTO sys_user (
    phone,
    password_hash,
    role,
    status,
    deleted,
    created_at,
    updated_at
) VALUES (
    '13800000001',
    '$2b$10$YM7V6DhqNgiFq1o14b10XOBey4TYfMydogGuSdI.th/EgLoRx2EYS',
    2,
    1,
    0,
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    role = 2,
    status = 1,
    deleted = 0,
    updated_at = NOW();

-- 新增或修复患者测试账号：13800000002 / 123456
INSERT INTO sys_user (
    phone,
    password_hash,
    role,
    status,
    deleted,
    created_at,
    updated_at
) VALUES (
    '13800000002',
    '$2b$10$YM7V6DhqNgiFq1o14b10XOBey4TYfMydogGuSdI.th/EgLoRx2EYS',
    3,
    1,
    0,
    NOW(),
    NOW()
)
ON DUPLICATE KEY UPDATE
    password_hash = VALUES(password_hash),
    role = 3,
    status = 1,
    deleted = 0,
    updated_at = NOW();

-- 补齐医生档案（医生端部分接口依赖 doctor_profile）
INSERT INTO doctor_profile (
    user_id,
    doctor_no,
    name,
    dept,
    title,
    intro,
    deleted,
    created_at,
    updated_at
)
SELECT
    u.id,
    'D13800000001',
    '测试医生',
    '全科',
    '主治医师',
    '系统初始化测试账号',
    0,
    NOW(),
    NOW()
FROM sys_user u
WHERE u.phone = '13800000001'
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    dept = VALUES(dept),
    title = VALUES(title),
    intro = VALUES(intro),
    deleted = 0,
    updated_at = NOW();

-- 补齐患者档案（患者端 /api/patient/me 依赖 patient_profile）
INSERT INTO patient_profile (
    user_id,
    name,
    gender,
    birthday,
    id_card_mask,
    deleted,
    created_at,
    updated_at
)
SELECT
    u.id,
    '测试患者',
    1,
    '1990-01-01',
    '110*************0002',
    0,
    NOW(),
    NOW()
FROM sys_user u
WHERE u.phone = '13800000002'
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    gender = VALUES(gender),
    birthday = VALUES(birthday),
    id_card_mask = VALUES(id_card_mask),
    deleted = 0,
    updated_at = NOW();
