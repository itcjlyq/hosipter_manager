package com.hospital.backend.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.backend.admin.dto.CreateDoctorRequest;
import com.hospital.backend.admin.dto.DoctorAdminVO;
import com.hospital.backend.admin.dto.ResetPasswordRequest;
import com.hospital.backend.admin.dto.UpdateDoctorRequest;
import com.hospital.backend.profile.entity.DoctorProfile;
import com.hospital.backend.profile.mapper.DoctorProfileMapper;
import com.hospital.backend.user.entity.SysUser;
import com.hospital.backend.user.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class AdminDoctorService {

    private static final int ROLE_DOCTOR = 2;

    private final SysUserMapper sysUserMapper;
    private final DoctorProfileMapper doctorProfileMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminDoctorService(SysUserMapper sysUserMapper, DoctorProfileMapper doctorProfileMapper,
                              PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.doctorProfileMapper = doctorProfileMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(rollbackFor = Exception.class)
    public DoctorAdminVO create(CreateDoctorRequest request) {
        String phone = request.phone().trim();
        if (existsPhone(phone)) {
            throw new IllegalArgumentException("手机号已存在");
        }
        String doctorNo = blankToNull(request.doctorNo());
        if (doctorNo != null && existsDoctorNo(doctorNo)) {
            throw new IllegalArgumentException("医生工号已被使用");
        }

        SysUser user = new SysUser();
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(ROLE_DOCTOR);
        user.setStatus(1);
        user.setDeleted(0);
        sysUserMapper.insert(user);

        DoctorProfile profile = new DoctorProfile();
        profile.setUserId(user.getId());
        profile.setDoctorNo(doctorNo);
        profile.setName(request.name().trim());
        profile.setDept(blankToNull(request.dept()));
        profile.setTitle(blankToNull(request.title()));
        profile.setIntro(request.intro());
        profile.setDeleted(0);
        doctorProfileMapper.insert(profile);

        return toVo(user, profile);
    }

    public List<DoctorAdminVO> list() {
        List<DoctorProfile> profiles = doctorProfileMapper.selectList(
                new LambdaQueryWrapper<DoctorProfile>()
                        .eq(DoctorProfile::getDeleted, 0)
                        .orderByDesc(DoctorProfile::getId)
        );
        return profiles.stream()
                .map(p -> {
                    SysUser u = sysUserMapper.selectById(p.getUserId());
                    if (u == null || u.getDeleted() != 0 || !Objects.equals(u.getRole(), ROLE_DOCTOR)) {
                        return null;
                    }
                    return toVo(u, p);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public DoctorAdminVO getByUserId(Long userId) {
        SysUser user = requireDoctorUser(userId);
        DoctorProfile profile = requireProfile(userId);
        return toVo(user, profile);
    }

    @Transactional(rollbackFor = Exception.class)
    public DoctorAdminVO update(Long userId, UpdateDoctorRequest request) {
        SysUser user = requireDoctorUser(userId);
        DoctorProfile profile = requireProfile(userId);

        String doctorNo = blankToNull(request.doctorNo());
        if (doctorNo != null && !doctorNo.equals(profile.getDoctorNo()) && existsDoctorNo(doctorNo)) {
            throw new IllegalArgumentException("医生工号已被使用");
        }
        profile.setDoctorNo(doctorNo);
        profile.setName(request.name().trim());
        profile.setDept(blankToNull(request.dept()));
        profile.setTitle(blankToNull(request.title()));
        profile.setIntro(request.intro());
        doctorProfileMapper.updateById(profile);

        if (request.status() != null) {
            if (request.status() != 0 && request.status() != 1) {
                throw new IllegalArgumentException("状态取值无效");
            }
            user.setStatus(request.status());
            sysUserMapper.updateById(user);
        }

        return toVo(sysUserMapper.selectById(userId), doctorProfileMapper.selectById(profile.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, ResetPasswordRequest request) {
        SysUser user = requireDoctorUser(userId);
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        sysUserMapper.updateById(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId) {
        SysUser user = requireDoctorUser(userId);
        DoctorProfile profile = requireProfile(userId);
        user.setDeleted(1);
        sysUserMapper.updateById(user);
        profile.setDeleted(1);
        doctorProfileMapper.updateById(profile);
    }

    private DoctorAdminVO toVo(SysUser user, DoctorProfile profile) {
        return new DoctorAdminVO(
                user.getId(),
                user.getPhone(),
                user.getStatus(),
                profile.getId(),
                profile.getDoctorNo(),
                profile.getName(),
                profile.getDept(),
                profile.getTitle(),
                profile.getIntro(),
                profile.getCreatedAt()
        );
    }

    private SysUser requireDoctorUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getDeleted() != 0 || !Objects.equals(user.getRole(), ROLE_DOCTOR)) {
            throw new IllegalArgumentException("医生不存在或已删除");
        }
        return user;
    }

    private DoctorProfile requireProfile(Long userId) {
        DoctorProfile profile = doctorProfileMapper.selectOne(
                new LambdaQueryWrapper<DoctorProfile>()
                        .eq(DoctorProfile::getUserId, userId)
                        .eq(DoctorProfile::getDeleted, 0)
                        .last("LIMIT 1")
        );
        if (profile == null) {
            throw new IllegalArgumentException("医生档案不存在或已删除");
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

    private boolean existsDoctorNo(String doctorNo) {
        Long c = doctorProfileMapper.selectCount(
                new LambdaQueryWrapper<DoctorProfile>()
                        .eq(DoctorProfile::getDoctorNo, doctorNo)
                        .eq(DoctorProfile::getDeleted, 0)
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
