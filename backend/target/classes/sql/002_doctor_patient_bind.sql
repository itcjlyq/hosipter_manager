USE hospital_service;

-- 医患绑定与审核：同一对医患在「未删除」状态下至多一条业务行，由应用层保证（可存在多条 deleted=1 的历史行）
CREATE TABLE IF NOT EXISTS doctor_patient_bind (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    doctor_user_id BIGINT NOT NULL COMMENT 'sys_user.id，role=doctor',
    patient_user_id BIGINT NOT NULL COMMENT 'sys_user.id，role=patient',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0-待审核,1-已通过,2-已拒绝',
    initiator TINYINT NOT NULL COMMENT '1-患者发起,2-医生发起',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_pair_active (doctor_user_id, patient_user_id, deleted),
    KEY idx_doctor_status (doctor_user_id, status, deleted),
    KEY idx_patient_status (patient_user_id, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医患绑定申请与关系';
