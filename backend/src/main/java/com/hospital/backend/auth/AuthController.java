package com.hospital.backend.auth;

import com.hospital.backend.auth.dto.LoginRequest;
import com.hospital.backend.auth.dto.LoginResponse;
import com.hospital.backend.common.ApiResponse;
import com.hospital.backend.user.entity.SysUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtTokenService jwtTokenService;
    private final AuthService authService;

    public AuthController(JwtTokenService jwtTokenService, AuthService authService) {
        this.jwtTokenService = jwtTokenService;
        this.authService = authService;
    }

    @PostMapping("/login/password")
    public ApiResponse<LoginResponse> loginByPassword(@RequestBody @Valid LoginRequest request) {
        SysUser user = authService.validateUser(request);
        if (user == null) {
            return ApiResponse.fail("手机号或密码错误");
        }

        String token = jwtTokenService.generate(String.valueOf(user.getId()), authService.roleToName(user.getRole()));
        long expiresAt = Instant.now().plusSeconds(jwtTokenService.expireSeconds()).getEpochSecond();
        return ApiResponse.ok(new LoginResponse(token, expiresAt));
    }
}
