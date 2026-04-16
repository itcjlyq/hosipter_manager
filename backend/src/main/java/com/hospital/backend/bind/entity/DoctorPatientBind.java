package com.hospital.backend.bind.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doctor_patient_bind")
public class DoctorPatientBind {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("doctor_user_id")
    private Long doctorUserId;

    @TableField("patient_user_id")
    private Long patientUserId;

    /** 0-待审核,1-已通过,2-已拒绝 */
    private Integer status;

    /** 1-患者发起,2-医生发起 */
    private Integer initiator;

    private Integer deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
