package com.hospital.backend.profile;

import com.hospital.backend.common.ApiResponse;
import com.hospital.backend.common.SecurityUtils;
import com.hospital.backend.profile.dto.PatientMeVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patient")
public class PatientMeController {

    private final PatientMeService patientMeService;

    public PatientMeController(PatientMeService patientMeService) {
        this.patientMeService = patientMeService;
    }

    /** 当前登录患者资料（姓名、脱敏手机、性别、年龄等） */
    @GetMapping("/me")
    public ApiResponse<PatientMeVO> me() {
        return ApiResponse.ok(patientMeService.getMe(SecurityUtils.requireUserId()));
    }
}
