package com.hospital.backend.bind.dto;

import jakarta.validation.constraints.NotNull;

public record ApplyBindRequest(@NotNull(message = "医生用户ID不能为空") Long doctorUserId) {
}
