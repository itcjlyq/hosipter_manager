package com.hospital.backend.profile.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.backend.profile.dto.DoctorDirectoryVO;
import com.hospital.backend.profile.entity.DoctorProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DoctorProfileMapper extends BaseMapper<DoctorProfile> {

    @Select("""
            SELECT dp.user_id AS userId, dp.name AS name, dp.dept AS dept, dp.title AS title, dp.doctor_no AS doctorNo
            FROM doctor_profile dp
            INNER JOIN sys_user u ON u.id = dp.user_id
            WHERE dp.deleted = 0 AND u.deleted = 0 AND u.status = 1 AND u.role = 2
            ORDER BY dp.id DESC
            """)
    List<DoctorDirectoryVO> selectActiveDoctorDirectory();
}
