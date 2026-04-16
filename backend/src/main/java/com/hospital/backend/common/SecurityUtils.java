package com.hospital.backend.common;

import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long requireUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new IllegalArgumentException("未登录");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("登录状态无效");
            }
        }
        throw new IllegalArgumentException("登录状态无效");
    }
}
