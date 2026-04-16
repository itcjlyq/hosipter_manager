package com.hospital.backend.bind.dto;

import java.time.LocalDateTime;

public record AdminBindItemVO(
        Long id,
        Long doctorUserId,
        Long patientUserId,
        String doctorName,
        String patientName,
        int status,
        int initiator,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
