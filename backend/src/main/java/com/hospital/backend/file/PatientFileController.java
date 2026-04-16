package com.hospital.backend.file;

import com.hospital.backend.common.ApiResponse;
import com.hospital.backend.common.SecurityUtils;
import com.hospital.backend.file.dto.FileAssetVO;
import com.hospital.backend.file.dto.FileDownloadView;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/patient/files")
public class PatientFileController {

    private final PatientFileAssetService fileAssetService;

    public PatientFileController(PatientFileAssetService fileAssetService) {
        this.fileAssetService = fileAssetService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<FileAssetVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bizType", required = false) String bizType) {
        Long patientUserId = SecurityUtils.requireUserId();
        return ApiResponse.ok(fileAssetService.uploadAsPatient(patientUserId, file, bizType));
    }

    @GetMapping
    public ApiResponse<List<FileAssetVO>> list() {
        Long patientUserId = SecurityUtils.requireUserId();
        return ApiResponse.ok(fileAssetService.listForPatient(patientUserId));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Long patientUserId = SecurityUtils.requireUserId();
        FileDownloadView view = fileAssetService.downloadForPatient(patientUserId, id);
        return toResourceResponse(view);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Long patientUserId = SecurityUtils.requireUserId();
        fileAssetService.softDeleteForPatient(patientUserId, id);
        return ApiResponse.ok(null);
    }

    private static ResponseEntity<Resource> toResourceResponse(FileDownloadView view) {
        MediaType mt = MediaType.APPLICATION_OCTET_STREAM;
        if (StringUtils.hasText(view.contentType())) {
            try {
                mt = MediaType.parseMediaType(view.contentType());
            } catch (Exception ignored) {
                // keep octet-stream
            }
        }
        ContentDisposition cd = ContentDisposition.attachment()
                .filename(view.originalName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(mt)
                .header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
                .body(view.resource());
    }
}
