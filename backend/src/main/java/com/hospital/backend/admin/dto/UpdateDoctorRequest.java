package com.hospital.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDoctorRequest(
        @NotBlank(message = "姓名不能为空") @Size(max = 64) String name,
        @Size(max = 64) String doctorNo,
        @Size(max = 64) String dept,
        @Size(max = 64) String title,
        String intro,
        Integer status
) {
}
