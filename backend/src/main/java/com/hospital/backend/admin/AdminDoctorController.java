package com.hospital.backend.admin;

import com.hospital.backend.admin.dto.CreateDoctorRequest;
import com.hospital.backend.admin.dto.DoctorAdminVO;
import com.hospital.backend.admin.dto.ResetPasswordRequest;
import com.hospital.backend.admin.dto.UpdateDoctorRequest;
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
@RequestMapping("/api/admin/doctors")
public class AdminDoctorController {

    private final AdminDoctorService adminDoctorService;

    public AdminDoctorController(AdminDoctorService adminDoctorService) {
        this.adminDoctorService = adminDoctorService;
    }

    @PostMapping
    public ApiResponse<DoctorAdminVO> create(@RequestBody @Valid CreateDoctorRequest request) {
        return ApiResponse.ok(adminDoctorService.create(request));
    }

    @GetMapping
    public ApiResponse<List<DoctorAdminVO>> list() {
        return ApiResponse.ok(adminDoctorService.list());
    }

    @GetMapping("/{userId}")
    public ApiResponse<DoctorAdminVO> get(@PathVariable Long userId) {
        return ApiResponse.ok(adminDoctorService.getByUserId(userId));
    }

    @PutMapping("/{userId}")
    public ApiResponse<DoctorAdminVO> update(@PathVariable Long userId,
                                             @RequestBody @Valid UpdateDoctorRequest request) {
        return ApiResponse.ok(adminDoctorService.update(userId, request));
    }

    @PostMapping("/{userId}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long userId,
                                           @RequestBody @Valid ResetPasswordRequest request) {
        adminDoctorService.resetPassword(userId, request);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<Void> delete(@PathVariable Long userId) {
        adminDoctorService.delete(userId);
        return ApiResponse.ok(null);
    }
}
