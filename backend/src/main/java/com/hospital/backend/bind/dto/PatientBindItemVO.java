package com.hospital.backend.bind.dto;

import java.time.LocalDateTime;

public record PatientBindItemVO(
        Long id,
        Long doctorUserId,
        String doctorName,
        String doctorDept,
        String doctorTitle,
        String doctorNo,
        int status,
        int initiator,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
