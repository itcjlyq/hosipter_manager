package com.hospital.backend.file;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.backend.bind.DoctorPatientBindService;
import com.hospital.backend.file.dto.FileAssetVO;
import com.hospital.backend.file.dto.FileDownloadView;
import com.hospital.backend.file.entity.PatientFileAsset;
import com.hospital.backend.file.mapper.PatientFileAssetMapper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class PatientFileAssetService {

    private static final int MAX_ORIGINAL_NAME = 255;
    private static final Pattern BIZ_TYPE = Pattern.compile("[a-z0-9_]{1,32}");

    private final PatientFileAssetMapper assetMapper;
    private final LocalFileStorage localFileStorage;
    private final DoctorPatientBindService bindService;

    public PatientFileAssetService(PatientFileAssetMapper assetMapper, LocalFileStorage localFileStorage,
                                   DoctorPatientBindService bindService) {
        this.assetMapper = assetMapper;
        this.localFileStorage = localFileStorage;
        this.bindService = bindService;
    }

    @Transactional(rollbackFor = Exception.class)
    public FileAssetVO uploadAsPatient(Long patientUserId, MultipartFile file, String bizType) {
        String bt = normalizeBizType(bizType);
        String rel = storeOrFail(file, bt);
        return insertRow(patientUserId, patientUserId, file, rel, bt);
    }

    @Transactional(rollbackFor = Exception.class)
    public FileAssetVO uploadAsDoctor(Long doctorUserId, Long patientUserId, MultipartFile file, String bizType) {
        bindService.requireApprovedBind(doctorUserId, patientUserId);
        String bt = normalizeBizType(bizType);
        String rel = storeOrFail(file, bt);
        return insertRow(patientUserId, doctorUserId, file, rel, bt);
    }

    public List<FileAssetVO> listForPatient(Long patientUserId) {
        List<PatientFileAsset> list = assetMapper.selectList(
                new LambdaQueryWrapper<PatientFileAsset>()
                        .eq(PatientFileAsset::getPatientUserId, patientUserId)
                        .eq(PatientFileAsset::getDeleted, 0)
                        .orderByDesc(PatientFileAsset::getId)
        );
        return list.stream().map(this::toVo).toList();
    }

    public List<FileAssetVO> listForDoctor(Long doctorUserId, Long patientUserId) {
        bindService.requireApprovedBind(doctorUserId, patientUserId);
        return listForPatient(patientUserId);
    }

    public FileDownloadView downloadForPatient(Long patientUserId, Long assetId) {
        PatientFileAsset a = requireActiveAsset(assetId);
        if (!Objects.equals(a.getPatientUserId(), patientUserId)) {
            throw new IllegalArgumentException("无权下载该文件");
        }
        return buildDownload(a);
    }

    public FileDownloadView downloadForDoctor(Long doctorUserId, Long assetId) {
        PatientFileAsset a = requireActiveAsset(assetId);
        bindService.requireApprovedBind(doctorUserId, a.getPatientUserId());
        return buildDownload(a);
    }

    @Transactional(rollbackFor = Exception.class)
    public void softDeleteForPatient(Long patientUserId, Long assetId) {
        PatientFileAsset a = requireActiveAsset(assetId);
        if (!Objects.equals(a.getPatientUserId(), patientUserId)) {
            throw new IllegalArgumentException("无权删除该文件");
        }
        markDeleted(a);
    }

    @Transactional(rollbackFor = Exception.class)
    public void softDeleteForDoctor(Long doctorUserId, Long assetId) {
        PatientFileAsset a = requireActiveAsset(assetId);
        bindService.requireApprovedBind(doctorUserId, a.getPatientUserId());
        markDeleted(a);
    }

    private void markDeleted(PatientFileAsset a) {
        a.setDeleted(1);
        a.setUpdatedAt(LocalDateTime.now());
        assetMapper.updateById(a);
    }

    private PatientFileAsset requireActiveAsset(Long id) {
        PatientFileAsset a = assetMapper.selectById(id);
        if (a == null || a.getDeleted() == null || a.getDeleted() != 0) {
            throw new IllegalArgumentException("文件不存在");
        }
        return a;
    }

    private FileDownloadView buildDownload(PatientFileAsset a) {
        var path = localFileStorage.resolveChecked(a.getStorageRelPath());
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("文件已丢失或已被清理");
        }
        Resource resource = new FileSystemResource(path);
        String ct = StringUtils.hasText(a.getContentType()) ? a.getContentType() : "application/octet-stream";
        return new FileDownloadView(resource, ct, a.getOriginalName());
    }

    private String storeOrFail(MultipartFile file, String bizType) {
        try {
            return localFileStorage.store(file, storageCategory(bizType));
        } catch (IOException e) {
            throw new IllegalArgumentException("保存文件失败，请稍后重试");
        }
    }

    /**
     * 诊断图片进 upload/images/；其它附件进 upload/files/（相对 app.storage.root）。
     */
    private static String storageCategory(String bizType) {
        if ("diagnosis_image".equals(bizType)) {
            return "images";
        }
        return "files";
    }

    private FileAssetVO insertRow(Long patientUserId, Long uploaderUserId, MultipartFile file, String rel,
                                  String bizType) {
        String original = truncate(safeOriginalName(file.getOriginalFilename()), MAX_ORIGINAL_NAME);
        String contentType = StringUtils.hasText(file.getContentType()) ? file.getContentType() : null;
        LocalDateTime now = LocalDateTime.now();
        PatientFileAsset row = new PatientFileAsset();
        row.setPatientUserId(patientUserId);
        row.setUploaderUserId(uploaderUserId);
        row.setOriginalName(original);
        row.setContentType(contentType);
        row.setSizeBytes(file.getSize());
        row.setStorageRelPath(rel);
        row.setBizType(bizType);
        row.setDeleted(0);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        assetMapper.insert(row);
        return toVo(row);
    }

    private static String normalizeBizType(String bizType) {
        if (!StringUtils.hasText(bizType)) {
            return "attachment";
        }
        String s = bizType.trim().toLowerCase(Locale.ROOT);
        if (!BIZ_TYPE.matcher(s).matches()) {
            throw new IllegalArgumentException("bizType 仅允许小写字母、数字、下划线，最长32位");
        }
        return s;
    }

    private static String safeOriginalName(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "unnamed";
        }
        String name = originalFilename.trim();
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        return name.isBlank() ? "unnamed" : name;
    }

    private static String truncate(String s, int max) {
        if (s.length() <= max) {
            return s;
        }
        return s.substring(0, max);
    }

    private FileAssetVO toVo(PatientFileAsset a) {
        return new FileAssetVO(
                a.getId(),
                a.getPatientUserId(),
                a.getUploaderUserId(),
                a.getOriginalName(),
                a.getContentType(),
                a.getSizeBytes() != null ? a.getSizeBytes() : 0L,
                a.getBizType(),
                a.getCreatedAt()
        );
    }
}
