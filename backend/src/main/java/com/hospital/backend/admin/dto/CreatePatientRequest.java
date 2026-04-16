package com.hospital.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePatientRequest(
        @NotBlank(message = "手机号不能为空") @Size(max = 20) String phone,
        @NotBlank(message = "密码不能为空") @Size(min = 6, max = 64) String password,
        @NotBlank(message = "姓名不能为空") @Size(max = 64) String name,
        Integer gender,
        LocalDate birthday,
        @Size(max = 32) String idCardMask
) {
}
