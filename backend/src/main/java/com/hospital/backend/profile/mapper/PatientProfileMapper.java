package com.hospital.backend.profile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.backend.profile.entity.PatientProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PatientProfileMapper extends BaseMapper<PatientProfile> {
}
