package com.hospital.backend.bind.dto;

import jakarta.validation.constraints.NotNull;

public record InvitePatientBindRequest(@NotNull(message = "患者用户ID不能为空") Long patientUserId) {
}
