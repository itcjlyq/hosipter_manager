package com.hospital.backend.profile.dto;

import lombok.Data;

@Data
public class DoctorDirectoryVO {

    private Long userId;
    private String name;
    private String dept;
    private String title;
    private String doctorNo;
}
