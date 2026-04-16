package com.hospital.backend.bind;

import com.hospital.backend.bind.dto.ApplyBindRequest;
import com.hospital.backend.bind.dto.PatientBindItemVO;
import com.hospital.backend.common.ApiResponse;
import com.hospital.backend.common.SecurityUtils;
import com.hospital.backend.profile.dto.DoctorDirectoryVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientBindController {

    private final DoctorPatientBindService bindService;

    public PatientBindController(DoctorPatientBindService bindService) {
        this.bindService = bindService;
    }

    /** 绑定页：可选医生列表（仅展示在职且档案存在的医生） */
    @GetMapping("/doctors")
    public ApiResponse<List<DoctorDirectoryVO>> listDoctors() {
        return ApiResponse.ok(bindService.listDoctorDirectory());
    }

    @PostMapping("/binds")
    public ApiResponse<PatientBindItemVO> apply(@RequestBody @Valid ApplyBindRequest request) {
        Long patientUserId = SecurityUtils.requireUserId();
        return ApiResponse.ok(bindService.patientApply(patientUserId, request));
    }

    @GetMapping("/binds")
    public ApiResponse<List<PatientBindItemVO>> listBinds() {
        Long patientUserId = SecurityUtils.requireUserId();
        return ApiResponse.ok(bindService.patientListBinds(patientUserId));
    }

    /** 取消待审核申请（软删除绑定行） */
    @DeleteMapping("/binds/{bindId}")
    public ApiResponse<Void> cancelPending(@PathVariable Long bindId) {
        Long patientUserId = SecurityUtils.requireUserId();
        bindService.patientCancelPending(patientUserId, bindId);
        return ApiResponse.ok(null);
    }
}
