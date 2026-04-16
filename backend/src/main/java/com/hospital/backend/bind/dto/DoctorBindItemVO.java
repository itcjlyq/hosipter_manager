package com.hospital.backend.bind.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DoctorBindItemVO(
        Long id,
        Long patientUserId,
        String patientName,
        int status,
        int initiator,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        /** 0-未知,1-男,2-女 */
        Integer patientGender,
        LocalDate patientBirthday,
        Integer patientAge,
        String patientPhoneMask
) {
}
