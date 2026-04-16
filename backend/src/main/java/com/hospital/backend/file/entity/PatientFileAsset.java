package com.hospital.backend.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("patient_file_asset")
public class PatientFileAsset {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("patient_user_id")
    private Long patientUserId;

    @TableField("uploader_user_id")
    private Long uploaderUserId;

    @TableField("original_name")
    private String originalName;

    @TableField("content_type")
    private String contentType;

    @TableField("size_bytes")
    private Long sizeBytes;

    @TableField("storage_rel_path")
    private String storageRelPath;

    @TableField("biz_type")
    private String bizType;

    private Integer deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
