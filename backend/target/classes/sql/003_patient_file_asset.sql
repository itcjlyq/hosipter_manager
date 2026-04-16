USE hospital_service;

-- 患者维度附件元数据；物理文件存本地目录（见 application.yml app.storage.root）
CREATE TABLE IF NOT EXISTS patient_file_asset (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    patient_user_id BIGINT NOT NULL COMMENT '文件归属患者 sys_user.id',
    uploader_user_id BIGINT NOT NULL COMMENT '实际上传人 sys_user.id',
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(128) NULL,
    size_bytes BIGINT NOT NULL,
    storage_rel_path VARCHAR(512) NOT NULL COMMENT '相对 app.storage.root 的路径',
    biz_type VARCHAR(32) NOT NULL DEFAULT 'attachment' COMMENT '业务分类：attachment/report/image 等',
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_patient_created (patient_user_id, deleted, created_at),
    KEY idx_uploader (uploader_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='患者附件（本地存储）';
