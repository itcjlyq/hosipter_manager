package com.hospital.backend.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.backend.user.entity.SysUser;
import com.hospital.backend.user.mapper.SysUserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.phone}")
    private String bootstrapAdminPhone;

    @Value("${app.bootstrap.admin.password}")
    private String bootstrapAdminPassword;

    public AdminUserInitializer(SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            SysUser exists = sysUserMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getPhone, bootstrapAdminPhone)
                            .eq(SysUser::getDeleted, 0)
                            .last("LIMIT 1")
            );
            if (exists != null) {
                return;
            }

            SysUser admin = new SysUser();
            admin.setPhone(bootstrapAdminPhone);
            admin.setPasswordHash(passwordEncoder.encode(bootstrapAdminPassword));
            admin.setRole(1);
            admin.setStatus(1);
            admin.setDeleted(0);
            sysUserMapper.insert(admin);
        } catch (DataAccessException e) {
            log.error(
                    "无法连接或访问 MySQL（初始化管理员账号失败）。请检查：1) 数据库进程是否已启动；"
                            + "2) application.yml 中 spring.datasource.url 的主机、端口、库名是否正确；"
                            + "3) 本机到该地址的网络/防火墙/安全组是否放行；"
                            + "4) 是否已建库并执行 resources/sql 下的基线脚本。本地开发可改为 jdbc:mysql://127.0.0.1:3306/hospital_service?...",
                    e);
            throw e;
        }
    }
}
