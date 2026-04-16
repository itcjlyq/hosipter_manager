package com.hospital.backend.profile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.backend.profile.dto.PatientMeVO;
import com.hospital.backend.profile.entity.PatientProfile;
import com.hospital.backend.profile.mapper.PatientProfileMapper;
import com.hospital.backend.user.entity.SysUser;
import com.hospital.backend.user.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;
@Service
public class PatientMeService {

    private static final int ROLE_PATIENT = 3;

    private final SysUserMapper sysUserMapper;
    private final PatientProfileMapper patientProfileMapper;

    public PatientMeService(SysUserMapper sysUserMapper, PatientProfileMapper patientProfileMapper) {
        this.sysUserMapper = sysUserMapper;
        this.patientProfileMapper = patientProfileMapper;
    }

    public PatientMeVO getMe(Long userId) {
        SysUser u = sysUserMapper.selectById(userId);
        if (u == null || u.getDeleted() == null || u.getDeleted() != 0
                || u.getStatus() == null || u.getStatus() != 1
                || u.getRole() == null || u.getRole() != ROLE_PATIENT) {
            throw new IllegalArgumentException("用户不存在或不是有效患者账号");
        }
        PatientProfile p = patientProfileMapper.selectOne(
                new LambdaQueryWrapper<PatientProfile>()
                        .eq(PatientProfile::getUserId, userId)
                        .eq(PatientProfile::getDeleted, 0)
                        .last("LIMIT 1")
        );
        if (p == null) {
            throw new IllegalArgumentException("患者档案不存在，请联系管理员完善资料");
        }
        LocalDate birthday = p.getBirthday();
        return new PatientMeVO(
                userId,
                maskPhone(u.getPhone()),
                p.getName(),
                p.getGender(),
                birthday,
                calcAge(birthday),
                p.getIdCardMask()
        );
    }

    private static String maskPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return "";
        }
        String t = phone.trim();
        if (t.length() >= 11) {
            return t.substring(0, 3) + "****" + t.substring(7);
        }
        if (t.length() >= 4) {
            return t.substring(0, 1) + "****" + t.substring(t.length() - 2);
        }
        return "****";
    }

    private static Integer calcAge(LocalDate birthday) {
        if (birthday == null) {
            return null;
        }
        return Period.between(birthday, LocalDate.now()).getYears();
    }
}
