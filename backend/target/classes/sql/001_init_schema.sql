CREATE DATABASE IF NOT EXISTS hospital_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE hospital_service;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role TINYINT NOT NULL COMMENT '1-admin,2-doctor,3-patient',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1-enable,0-disable',
    last_login_at DATETIME NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_phone (phone),
    KEY idx_role_status (role, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS doctor_profile (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '关联 sys_user.id，role=doctor',
    doctor_no VARCHAR(64) NULL COMMENT '工号，可选',
    name VARCHAR(64) NOT NULL,
    dept VARCHAR(64) NULL,
    title VARCHAR(64) NULL,
    intro TEXT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_doctor_user (user_id),
    UNIQUE KEY uk_doctor_no (doctor_no),
    KEY idx_doctor_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS patient_profile (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '关联 sys_user.id，role=patient',
    name VARCHAR(64) NOT NULL,
    gender TINYINT NULL COMMENT '0-未知,1-男,2-女',
    birthday DATE NULL,
    id_card_mask VARCHAR(32) NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_patient_user (user_id),
    KEY idx_patient_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
