/*
 Navicat Premium Dump SQL

 Source Server         : hospital
 Source Server Type    : MySQL
 Source Server Version : 80027 (8.0.27)
 Source Host           : 8.148.155.226:3306
 Source Schema         : hospital_service

 Target Server Type    : MySQL
 Target Server Version : 80027 (8.0.27)
 File Encoding         : 65001

 Date: 13/04/2026 18:59:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for doctor_patient_bind
-- ----------------------------
DROP TABLE IF EXISTS `doctor_patient_bind`;
CREATE TABLE `doctor_patient_bind`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `doctor_user_id` bigint NOT NULL COMMENT 'sys_user.id，role=doctor',
  `patient_user_id` bigint NOT NULL COMMENT 'sys_user.id，role=patient',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0-待审核,1-已通过,2-已拒绝',
  `initiator` tinyint NOT NULL COMMENT '1-患者发起,2-医生发起',
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pair_active`(`doctor_user_id` ASC, `patient_user_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_doctor_status`(`doctor_user_id` ASC, `status` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_patient_status`(`patient_user_id` ASC, `status` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医患绑定申请与关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of doctor_patient_bind
-- ----------------------------
INSERT INTO `doctor_patient_bind` VALUES (1, 2, 3, 1, 1, 0, '2026-04-13 17:02:33', '2026-04-13 17:02:58');

-- ----------------------------
-- Table structure for doctor_profile
-- ----------------------------
DROP TABLE IF EXISTS `doctor_profile`;
CREATE TABLE `doctor_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '关联 sys_user.id，role=doctor',
  `doctor_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工号，可选',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dept` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_doctor_user`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_doctor_no`(`doctor_no` ASC) USING BTREE,
  INDEX `idx_doctor_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of doctor_profile
-- ----------------------------
INSERT INTO `doctor_profile` VALUES (1, 2, 'D13800000001', '测试医生', '全科', '主治医师', '系统初始化测试账号', 0, '2026-04-13 08:25:06', '2026-04-13 08:25:06');

-- ----------------------------
-- Table structure for patient_file_asset
-- ----------------------------
DROP TABLE IF EXISTS `patient_file_asset`;
CREATE TABLE `patient_file_asset`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `patient_user_id` bigint NOT NULL COMMENT '文件归属患者 sys_user.id',
  `uploader_user_id` bigint NOT NULL COMMENT '实际上传人 sys_user.id',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `size_bytes` bigint NOT NULL,
  `storage_rel_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '相对 app.storage.root 的路径',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'attachment' COMMENT '业务分类：attachment/report/image 等',
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_patient_created`(`patient_user_id` ASC, `deleted` ASC, `created_at` ASC) USING BTREE,
  INDEX `idx_uploader`(`uploader_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '患者附件（本地存储）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of patient_file_asset
-- ----------------------------

-- ----------------------------
-- Table structure for patient_profile
-- ----------------------------
DROP TABLE IF EXISTS `patient_profile`;
CREATE TABLE `patient_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '关联 sys_user.id，role=patient',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `gender` tinyint NULL DEFAULT NULL COMMENT '0-未知,1-男,2-女',
  `birthday` date NULL DEFAULT NULL,
  `id_card_mask` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_patient_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_patient_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of patient_profile
-- ----------------------------
INSERT INTO `patient_profile` VALUES (1, 3, '测试患者', 1, '1990-01-01', '110*************0002', 0, '2026-04-13 08:25:06', '2026-04-13 08:25:06');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role` tinyint NOT NULL COMMENT '1-admin,2-doctor,3-patient',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '1-enable,0-disable',
  `last_login_at` datetime NULL DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_role_status`(`role` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, '13800000000', '$2b$10$pWvE6JvcQAnmRWGWg.oLIuAS0lTQqGhNBQQWLCjP9vQkVFfz0.PxO', 1, 1, '2026-04-13 18:45:01', 0, '2026-04-13 08:25:06', '2026-04-13 08:25:06');
INSERT INTO `sys_user` VALUES (2, '13800000001', '$2b$10$YM7V6DhqNgiFq1o14b10XOBey4TYfMydogGuSdI.th/EgLoRx2EYS', 2, 1, '2026-04-13 18:41:14', 0, '2026-04-13 08:25:06', '2026-04-13 08:25:06');
INSERT INTO `sys_user` VALUES (3, '13800000002', '$2b$10$YM7V6DhqNgiFq1o14b10XOBey4TYfMydogGuSdI.th/EgLoRx2EYS', 3, 1, '2026-04-13 18:44:32', 0, '2026-04-13 08:25:06', '2026-04-13 08:25:06');

SET FOREIGN_KEY_CHECKS = 1;
