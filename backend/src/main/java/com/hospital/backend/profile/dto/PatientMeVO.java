package com.hospital.backend.profile.dto;

import java.time.LocalDate;

/**
 * 当前登录患者的基础资料（患者端「我的」与首页展示）。
 */
public record PatientMeVO(
        Long userId,
        String phoneMask,
        String name,
        Integer gender,
        LocalDate birthday,
        Integer age,
        String idCardMask
) {
}
