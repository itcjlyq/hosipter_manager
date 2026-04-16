package com.hospital.backend.admin.dto;

import java.time.LocalDateTime;

public record DoctorAdminVO(
        Long userId,
        String phone,
        Integer status,
        Long profileId,
        String doctorNo,
        String name,
        String dept,
        String title,
        String intro,
        LocalDateTime createdAt
) {
}
