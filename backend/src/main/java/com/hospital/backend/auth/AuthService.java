package com.hospital.backend.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.backend.auth.dto.LoginRequest;
import com.hospital.backend.user.entity.SysUser;
import com.hospital.backend.user.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthService(SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public SysUser validateUser(LoginRequest request) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhone, request.phone())
                        .eq(SysUser::getDeleted, 0)
                        .last("LIMIT 1")
        );
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            return null;
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            return null;
        }

        user.setLastLoginAt(LocalDateTime.now());
        sysUserMapper.updateById(user);
        return user;
    }

    public String roleToName(Integer role) {
        if (role == null) {
            return "unknown";
        }
        return switch (role) {
            case 1 -> "admin";
            case 2 -> "doctor";
            case 3 -> "patient";
            default -> "unknown";
        };
    }
}
