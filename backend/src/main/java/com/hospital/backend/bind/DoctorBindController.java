package com.hospital.backend.bind;

import com.hospital.backend.bind.dto.DoctorBindItemVO;
import com.hospital.backend.bind.dto.InvitePatientBindRequest;
import com.hospital.backend.common.ApiResponse;
import com.hospital.backend.common.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorBindController {

    private final DoctorPatientBindService bindService;

    public DoctorBindController(DoctorPatientBindService bindService) {
        this.bindService = bindService;
    }

    /**
     * 绑定列表。status 可选：pending / approved / rejected；不传则返回全部未删除记录。
     */
    @GetMapping("/binds")
    public ApiResponse<List<DoctorBindItemVO>> list(
            @RequestParam(name = "status", required = false) String status) {
        Long doctorUserId = SecurityUtils.requireUserId();
        return ApiResponse.ok(bindService.doctorListBinds(doctorUserId, status));
    }

    @PostMapping("/binds/invite")
    public ApiResponse<DoctorBindItemVO> invite(@RequestBody @Valid InvitePatientBindRequest request) {
        Long doctorUserId = SecurityUtils.requireUserId();
        return ApiResponse.ok(bindService.doctorInvite(doctorUserId, request));
    }

    @PostMapping("/binds/{bindId}/approve")
    public ApiResponse<Void> approve(@PathVariable Long bindId) {
        Long doctorUserId = SecurityUtils.requireUserId();
        bindService.doctorApprove(doctorUserId, bindId);
        return ApiResponse.ok(null);
    }

    @PostMapping("/binds/{bindId}/reject")
    public ApiResponse<Void> reject(@PathVariable Long bindId) {
        Long doctorUserId = SecurityUtils.requireUserId();
        bindService.doctorReject(doctorUserId, bindId);
        return ApiResponse.ok(null);
    }
}
