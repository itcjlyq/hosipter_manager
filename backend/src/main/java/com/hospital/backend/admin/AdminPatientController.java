package com.hospital.backend.admin;

import com.hospital.backend.admin.dto.CreatePatientRequest;
import com.hospital.backend.admin.dto.PatientAdminVO;
import com.hospital.backend.admin.dto.ResetPasswordRequest;
import com.hospital.backend.admin.dto.UpdatePatientRequest;
import com.hospital.backend.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/patients")
public class AdminPatientController {

    private final AdminPatientService adminPatientService;

    public AdminPatientController(AdminPatientService adminPatientService) {
        this.adminPatientService = adminPatientService;
    }

    @PostMapping
    public ApiResponse<PatientAdminVO> create(@RequestBody @Valid CreatePatientRequest request) {
        return ApiResponse.ok(adminPatientService.create(request));
    }

    @GetMapping
    public ApiResponse<List<PatientAdminVO>> list() {
        return ApiResponse.ok(adminPatientService.list());
    }

    @GetMapping("/{userId}")
    public ApiResponse<PatientAdminVO> get(@PathVariable Long userId) {
        return ApiResponse.ok(adminPatientService.getByUserId(userId));
    }

    @PutMapping("/{userId}")
    public ApiResponse<PatientAdminVO> update(@PathVariable Long userId,
                                              @RequestBody @Valid UpdatePatientRequest request) {
        return ApiResponse.ok(adminPatientService.update(userId, request));
    }

    @PostMapping("/{userId}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long userId,
                                           @RequestBody @Valid ResetPasswordRequest request) {
        adminPatientService.resetPassword(userId, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<Void> delete(@PathVariable Long userId) {
        adminPatientService.delete(userId);
        return ApiResponse.ok(null);
    }
}
