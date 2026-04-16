package com.hospital.backend.admin.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientAdminVO(
        Long userId,
        String phone,
        Integer status,
        Long profileId,
        String name,
        Integer gender,
        LocalDate birthday,
        String idCardMask,
        LocalDateTime createdAt
) {
}
