package com.hospital.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdatePatientRequest(
        @NotBlank(message = "姓名不能为空") @Size(max = 64) String name,
        Integer gender,
        LocalDate birthday,
        @Size(max = 32) String idCardMask,
        Integer status
) {
}
