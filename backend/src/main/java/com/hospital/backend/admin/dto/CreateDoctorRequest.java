package com.hospital.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDoctorRequest(
        @NotBlank(message = "手机号不能为空") @Size(max = 20) String phone,
        @NotBlank(message = "密码不能为空") @Size(min = 6, max = 64) String password,
        @NotBlank(message = "姓名不能为空") @Size(max = 64) String name,
        @Size(max = 64) String doctorNo,
        @Size(max = 64) String dept,
        @Size(max = 64) String title,
        String intro
) {
}
