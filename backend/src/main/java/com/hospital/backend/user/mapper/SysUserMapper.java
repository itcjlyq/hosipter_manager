package com.hospital.backend.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.backend.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
