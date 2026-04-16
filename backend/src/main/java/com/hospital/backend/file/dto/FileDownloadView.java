package com.hospital.backend.file.dto;

import org.springframework.core.io.Resource;

public record FileDownloadView(Resource resource, String contentType, String originalName) {
}
