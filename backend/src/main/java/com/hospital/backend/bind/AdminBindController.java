package com.hospital.backend.bind;

import com.hospital.backend.bind.dto.AdminBindItemVO;
import com.hospital.backend.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/binds")
public class AdminBindController {

    private final DoctorPatientBindService bindService;

    public AdminBindController(DoctorPatientBindService bindService) {
        this.bindService = bindService;
    }

    /**
     * 管理端查看绑定记录。status 可选：pending / approved / rejected；不传则全部。
     */
    @GetMapping
    public ApiResponse<List<AdminBindItemVO>> list(
            @RequestParam(name = "status", required = false) String status) {
        return ApiResponse.ok(bindService.adminListBinds(status));
    }
}
