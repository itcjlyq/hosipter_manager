package com.hospital.backend.file.dto;

import java.time.LocalDateTime;

public record FileAssetVO(
        Long id,
        Long patientUserId,
        Long uploaderUserId,
        String originalName,
        String contentType,
        long sizeBytes,
        String bizType,
        LocalDateTime createdAt
) {
}
