package com.hospital.backend.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.backend.admin.dto.CreatePatientRequest;
import com.hospital.backend.admin.dto.PatientAdminVO;
import com.hospital.backend.admin.dto.ResetPasswordRequest;
import com.hospital.backend.admin.dto.UpdatePatientRequest;
import com.hospital.backend.profile.entity.PatientProfile;
import com.hospital.backend.profile.mapper.PatientProfileMapper;
import com.hospital.backend.user.entity.SysUser;
import com.hospital.backend.user.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class AdminPatientService {

    private static final int ROLE_PATIENT = 3;

    private final SysUserMapper sysUserMapper;
    private final PatientProfileMapper patientProfileMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminPatientService(SysUserMapper sysUserMapper, PatientProfileMapper patientProfileMapper,
                               PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.patientProfileMapper = patientProfileMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(rollbackFor = Exception.class)
    public PatientAdminVO create(CreatePatientRequest request) {
        String phone = request.phone().trim();
        if (existsPhone(phone)) {
            throw new IllegalArgumentException("手机号已存在");
        }

        SysUser user = new SysUser();
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(ROLE_PATIENT);
        user.setStatus(1);
        user.setDeleted(0);
        sysUserMapper.insert(user);

        PatientProfile profile = new PatientProfile();
        profile.setUserId(user.getId());
        profile.setName(request.name().trim());
        profile.setGender(request.gender());
        profile.setBirthday(request.birthday());
        profile.setIdCardMask(blankToNull(request.idCardMask()));
        profile.setDeleted(0);
        patientProfileMapper.insert(profile);

        return toVo(user, profile);
    }

    public List<PatientAdminVO> list() {
        List<PatientProfile> profiles = patientProfileMapper.selectList(
                new LambdaQueryWrapper<PatientProfile>()
                        .eq(PatientProfile::getDeleted, 0)
                        .orderByDesc(PatientProfile::getId)
        );
        return profiles.stream()
                .map(p -> {
                    SysUser u = sysUserMapper.selectById(p.getUserId());
                    if (u == null || u.getDeleted() != 0 || !Objects.equals(u.getRole(), ROLE_PATIENT)) {
                        return null;
                    }
                    return toVo(u, p);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public PatientAdminVO getByUserId(Long userId) {
        SysUser user = requirePatientUser(userId);
        PatientProfile profile = requireProfile(userId);
        return toVo(user, profile);
    }

    @Transactional(rollbackFor = Exception.class)
    public PatientAdminVO update(Long userId, UpdatePatientRequest request) {
        SysUser user = requirePatientUser(userId);
        PatientProfile profile = requireProfile(userId);

        profile.setName(request.name().trim());
        profile.setGender(request.gender());
        profile.setBirthday(request.birthday());
        profile.setIdCardMask(blankToNull(request.idCardMask()));
        patientProfileMapper.updateById(profile);

        if (request.status() != null) {
            if (request.status() != 0 && request.status() != 1) {
                throw new IllegalArgumentException("状态取值无效");
            }
            user.setStatus(request.status());
            sysUserMapper.updateById(user);
        }

        return toVo(sysUserMapper.selectById(userId), patientProfileMapper.selectById(profile.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, ResetPasswordRequest request) {
        SysUser user = requirePatientUser(userId);
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        sysUserMapper.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId) {
        SysUser user = requirePatientUser(userId);
        PatientProfile profile = requireProfile(userId);
        user.setDeleted(1);
        sysUserMapper.updateById(user);
        profile.setDeleted(1);
        patientProfileMapper.updateById(profile);
    }

    private PatientAdminVO toVo(SysUser user, PatientProfile profile) {
        return new PatientAdminVO(
                user.getId(),
                user.getPhone(),
                user.getStatus(),
                profile.getId(),
                profile.getName(),
                profile.getGender(),
                profile.getBirthday(),
                profile.getIdCardMask(),
                profile.getCreatedAt()
        );
    }

    private SysUser requirePatientUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getDeleted() != 0 || !Objects.equals(user.getRole(), ROLE_PATIENT)) {
            throw new IllegalArgumentException("患者不存在或已删除");
        }
        return user;
    }

    private PatientProfile requireProfile(Long userId) {
        PatientProfile profile = patientProfileMapper.selectOne(
                new LambdaQueryWrapper<PatientProfile>()
                        .eq(PatientProfile::getUserId, userId)
                        .eq(PatientProfile::getDeleted, 0)
                        .last("LIMIT 1")
        );
        if (profile == null) {
            throw new IllegalArgumentException("患者档案不存在或已删除");
        }
        return profile;
    }

    private boolean existsPhone(String phone) {
        Long c = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhone, phone)
                        .eq(SysUser::getDeleted, 0)
        );
        return c != null && c > 0;
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
