package com.hospital.backend.bind.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.backend.bind.entity.DoctorPatientBind;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DoctorPatientBindMapper extends BaseMapper<DoctorPatientBind> {
}
