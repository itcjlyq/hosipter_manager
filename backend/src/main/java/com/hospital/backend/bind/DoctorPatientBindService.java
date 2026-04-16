package com.hospital.backend.bind;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hospital.backend.bind.dto.AdminBindItemVO;
import com.hospital.backend.bind.dto.ApplyBindRequest;
import com.hospital.backend.bind.dto.DoctorBindItemVO;
import com.hospital.backend.bind.dto.InvitePatientBindRequest;
import com.hospital.backend.bind.dto.PatientBindItemVO;
import com.hospital.backend.bind.entity.DoctorPatientBind;
import com.hospital.backend.bind.mapper.DoctorPatientBindMapper;
import com.hospital.backend.profile.dto.DoctorDirectoryVO;
import com.hospital.backend.profile.entity.DoctorProfile;
import com.hospital.backend.profile.entity.PatientProfile;
import com.hospital.backend.profile.mapper.DoctorProfileMapper;
import com.hospital.backend.profile.mapper.PatientProfileMapper;
import com.hospital.backend.user.entity.SysUser;
import com.hospital.backend.user.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DoctorPatientBindService {

    private static final int ROLE_DOCTOR = 2;
    private static final int ROLE_PATIENT = 3;

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_REJECTED = 2;

    private static final int INITIATOR_PATIENT = 1;
    private static final int INITIATOR_DOCTOR = 2;

    private final DoctorPatientBindMapper bindMapper;
    private final SysUserMapper sysUserMapper;
    private final DoctorProfileMapper doctorProfileMapper;
    private final PatientProfileMapper patientProfileMapper;

    public DoctorPatientBindService(DoctorPatientBindMapper bindMapper, SysUserMapper sysUserMapper,
                                    DoctorProfileMapper doctorProfileMapper,
                                    PatientProfileMapper patientProfileMapper) {
        this.bindMapper = bindMapper;
        this.sysUserMapper = sysUserMapper;
        this.doctorProfileMapper = doctorProfileMapper;
        this.patientProfileMapper = patientProfileMapper;
    }

    public List<DoctorDirectoryVO> listDoctorDirectory() {
        return doctorProfileMapper.selectActiveDoctorDirectory();
    }

    @Transactional(rollbackFor = Exception.class)
    public PatientBindItemVO patientApply(Long patientUserId, ApplyBindRequest request) {
        Long doctorUserId = request.doctorUserId();
        requireActiveUserRole(patientUserId, ROLE_PATIENT);
        requireActiveDoctorProfile(doctorUserId);
        if (doctorUserId.equals(patientUserId)) {
            throw new IllegalArgumentException("不能绑定本人账号");
        }

        LocalDateTime now = LocalDateTime.now();
        DoctorPatientBind active = findActiveBind(doctorUserId, patientUserId);
        if (active == null) {
            DoctorPatientBind row = newRow(doctorUserId, patientUserId, STATUS_PENDING, INITIATOR_PATIENT, now);
            bindMapper.insert(row);
            return toPatientItem(row, loadDoctorProfilesByUserIds(Set.of(doctorUserId)));
        }
        if (active.getStatus() == STATUS_APPROVED) {
            throw new IllegalArgumentException("已与该医生建立绑定");
        }
        if (active.getStatus() == STATUS_PENDING) {
            throw new IllegalArgumentException("与该医生的申请尚在审核中");
        }
        active.setStatus(STATUS_PENDING);
        active.setInitiator(INITIATOR_PATIENT);
        active.setUpdatedAt(now);
        bindMapper.updateById(active);
        return toPatientItem(active, loadDoctorProfilesByUserIds(Set.of(doctorUserId)));
    }

    @Transactional(rollbackFor = Exception.class)
    public void patientCancelPending(Long patientUserId, Long bindId) {
        DoctorPatientBind b = bindMapper.selectById(bindId);
        if (b == null || b.getDeleted() == null || b.getDeleted() != 0) {
            throw new IllegalArgumentException("绑定记录不存在");
        }
        if (!Objects.equals(b.getPatientUserId(), patientUserId)) {
            throw new IllegalArgumentException("无权操作该绑定");
        }
        if (b.getStatus() == null || b.getStatus() != STATUS_PENDING) {
            throw new IllegalArgumentException("仅待审核的申请可取消");
        }
        b.setDeleted(1);
        b.setUpdatedAt(LocalDateTime.now());
        bindMapper.updateById(b);
    }

    public List<PatientBindItemVO> patientListBinds(Long patientUserId) {
        List<DoctorPatientBind> list = bindMapper.selectList(
                new LambdaQueryWrapper<DoctorPatientBind>()
                        .eq(DoctorPatientBind::getPatientUserId, patientUserId)
                        .eq(DoctorPatientBind::getDeleted, 0)
                        .orderByDesc(DoctorPatientBind::getUpdatedAt)
        );
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> doctorIds = list.stream().map(DoctorPatientBind::getDoctorUserId).collect(Collectors.toSet());
        Map<Long, DoctorProfile> docMap = loadDoctorProfilesByUserIds(doctorIds);
        return list.stream().map(b -> toPatientItem(b, docMap)).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public DoctorBindItemVO doctorInvite(Long doctorUserId, InvitePatientBindRequest request) {
        Long patientUserId = request.patientUserId();
        requireActiveUserRole(doctorUserId, ROLE_DOCTOR);
        requireActiveDoctorProfile(doctorUserId);
        requireActiveUserRole(patientUserId, ROLE_PATIENT);
        requireActivePatientProfile(patientUserId);
        if (doctorUserId.equals(patientUserId)) {
            throw new IllegalArgumentException("不能绑定本人账号");
        }

        LocalDateTime now = LocalDateTime.now();
        DoctorPatientBind active = findActiveBind(doctorUserId, patientUserId);
        if (active == null) {
            DoctorPatientBind row = newRow(doctorUserId, patientUserId, STATUS_PENDING, INITIATOR_DOCTOR, now);
            bindMapper.insert(row);
            return toDoctorItem(row, loadPatientProfilesByUserIds(Set.of(patientUserId)),
                    loadUsersByIds(Set.of(patientUserId)));
        }
        if (active.getStatus() == STATUS_APPROVED) {
            throw new IllegalArgumentException("已与该患者建立绑定");
        }
        if (active.getStatus() == STATUS_PENDING) {
            throw new IllegalArgumentException("与该患者的申请尚在审核中");
        }
        active.setStatus(STATUS_PENDING);
        active.setInitiator(INITIATOR_DOCTOR);
        active.setUpdatedAt(now);
        bindMapper.updateById(active);
        return toDoctorItem(active, loadPatientProfilesByUserIds(Set.of(patientUserId)),
                loadUsersByIds(Set.of(patientUserId)));
    }

    public List<DoctorBindItemVO> doctorListBinds(Long doctorUserId, String statusFilter) {
        LambdaQueryWrapper<DoctorPatientBind> q = new LambdaQueryWrapper<DoctorPatientBind>()
                .eq(DoctorPatientBind::getDoctorUserId, doctorUserId)
                .eq(DoctorPatientBind::getDeleted, 0);
        applyStatusFilter(q, statusFilter);
        q.orderByDesc(DoctorPatientBind::getUpdatedAt);
        List<DoctorPatientBind> list = bindMapper.selectList(q);
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> patientIds = list.stream().map(DoctorPatientBind::getPatientUserId).collect(Collectors.toSet());
        Map<Long, PatientProfile> pmap = loadPatientProfilesByUserIds(patientIds);
        Map<Long, SysUser> umap = loadUsersByIds(patientIds);
        return list.stream().map(b -> toDoctorItem(b, pmap, umap)).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void doctorApprove(Long doctorUserId, Long bindId) {
        DoctorPatientBind b = requireOwnedBindForDoctor(bindId, doctorUserId);
        if (b.getStatus() == null || b.getStatus() != STATUS_PENDING) {
            throw new IllegalArgumentException("当前状态不可通过审核");
        }
        b.setStatus(STATUS_APPROVED);
        b.setUpdatedAt(LocalDateTime.now());
        bindMapper.updateById(b);
    }

    @Transactional(rollbackFor = Exception.class)
    public void doctorReject(Long doctorUserId, Long bindId) {
        DoctorPatientBind b = requireOwnedBindForDoctor(bindId, doctorUserId);
        if (b.getStatus() == null || b.getStatus() != STATUS_PENDING) {
            throw new IllegalArgumentException("当前状态不可拒绝");
        }
        b.setStatus(STATUS_REJECTED);
        b.setUpdatedAt(LocalDateTime.now());
        bindMapper.updateById(b);
    }

    public List<AdminBindItemVO> adminListBinds(String statusFilter) {
        LambdaQueryWrapper<DoctorPatientBind> q = new LambdaQueryWrapper<DoctorPatientBind>()
                .eq(DoctorPatientBind::getDeleted, 0);
        applyStatusFilter(q, statusFilter);
        q.orderByDesc(DoctorPatientBind::getUpdatedAt);
        List<DoctorPatientBind> list = bindMapper.selectList(q);
        if (list.isEmpty()) {
            return List.of();
        }
        Set<Long> doctorIds = list.stream().map(DoctorPatientBind::getDoctorUserId).collect(Collectors.toSet());
        Set<Long> patientIds = list.stream().map(DoctorPatientBind::getPatientUserId).collect(Collectors.toSet());
        Map<Long, DoctorProfile> dmap = loadDoctorProfilesByUserIds(doctorIds);
        Map<Long, PatientProfile> pmap = loadPatientProfilesByUserIds(patientIds);
        return list.stream().map(b -> toAdminItem(b, dmap, pmap)).toList();
    }

    /**
     * 医生与患者之间必须存在「已审核通过」且未删除的绑定，否则抛出 {@link IllegalArgumentException}。
     */
    public void requireApprovedBind(Long doctorUserId, Long patientUserId) {
        DoctorPatientBind b = findActiveBind(doctorUserId, patientUserId);
        if (b == null || b.getStatus() == null || b.getStatus() != STATUS_APPROVED) {
            throw new IllegalArgumentException("无已审核通过的医患绑定，无法操作");
        }
    }

    private static void applyStatusFilter(LambdaQueryWrapper<DoctorPatientBind> q, String statusFilter) {
        if (!StringUtils.hasText(statusFilter)) {
            return;
        }
        String s = statusFilter.trim().toLowerCase();
        switch (s) {
            case "pending" -> q.eq(DoctorPatientBind::getStatus, STATUS_PENDING);
            case "approved" -> q.eq(DoctorPatientBind::getStatus, STATUS_APPROVED);
            case "rejected" -> q.eq(DoctorPatientBind::getStatus, STATUS_REJECTED);
            default -> {
            }
        }
    }

    private DoctorPatientBind findActiveBind(Long doctorUserId, Long patientUserId) {
        return bindMapper.selectOne(
                new LambdaQueryWrapper<DoctorPatientBind>()
                        .eq(DoctorPatientBind::getDoctorUserId, doctorUserId)
                        .eq(DoctorPatientBind::getPatientUserId, patientUserId)
                        .eq(DoctorPatientBind::getDeleted, 0)
                        .last("LIMIT 1")
        );
    }

    private DoctorPatientBind newRow(Long doctorUserId, Long patientUserId, int status, int initiator,
                                     LocalDateTime now) {
        DoctorPatientBind row = new DoctorPatientBind();
        row.setDoctorUserId(doctorUserId);
        row.setPatientUserId(patientUserId);
        row.setStatus(status);
        row.setInitiator(initiator);
        row.setDeleted(0);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        return row;
    }

    private DoctorPatientBind requireOwnedBindForDoctor(Long bindId, Long doctorUserId) {
        DoctorPatientBind b = bindMapper.selectById(bindId);
        if (b == null || b.getDeleted() == null || b.getDeleted() != 0) {
            throw new IllegalArgumentException("绑定记录不存在");
        }
        if (!Objects.equals(b.getDoctorUserId(), doctorUserId)) {
            throw new IllegalArgumentException("无权操作该绑定");
        }
        return b;
    }

    private void requireActiveUserRole(Long userId, int role) {
        SysUser u = sysUserMapper.selectById(userId);
        if (u == null || u.getDeleted() == null || u.getDeleted() != 0
                || u.getStatus() == null || u.getStatus() != 1
                || u.getRole() == null || u.getRole() != role) {
            throw new IllegalArgumentException("用户不存在或类型不匹配");
        }
    }

    private void requireActiveDoctorProfile(Long doctorUserId) {
        requireActiveUserRole(doctorUserId, ROLE_DOCTOR);
        DoctorProfile dp = doctorProfileMapper.selectOne(
                new LambdaQueryWrapper<DoctorProfile>()
                        .eq(DoctorProfile::getUserId, doctorUserId)
                        .eq(DoctorProfile::getDeleted, 0)
                        .last("LIMIT 1")
        );
        if (dp == null) {
            throw new IllegalArgumentException("医生档案不存在");
        }
    }

    private void requireActivePatientProfile(Long patientUserId) {
        PatientProfile pp = patientProfileMapper.selectOne(
                new LambdaQueryWrapper<PatientProfile>()
                        .eq(PatientProfile::getUserId, patientUserId)
                        .eq(PatientProfile::getDeleted, 0)
                        .last("LIMIT 1")
        );
        if (pp == null) {
            throw new IllegalArgumentException("患者档案不存在");
        }
    }

    private Map<Long, DoctorProfile> loadDoctorProfilesByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<DoctorProfile> list = doctorProfileMapper.selectList(
                new LambdaQueryWrapper<DoctorProfile>()
                        .in(DoctorProfile::getUserId, userIds)
                        .eq(DoctorProfile::getDeleted, 0)
        );
        return list.stream().collect(Collectors.toMap(DoctorProfile::getUserId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, PatientProfile> loadPatientProfilesByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<PatientProfile> list = patientProfileMapper.selectList(
                new LambdaQueryWrapper<PatientProfile>()
                        .in(PatientProfile::getUserId, userIds)
                        .eq(PatientProfile::getDeleted, 0)
        );
        return list.stream().collect(Collectors.toMap(PatientProfile::getUserId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, SysUser> loadUsersByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        List<SysUser> list = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .in(SysUser::getId, userIds)
                        .eq(SysUser::getDeleted, 0)
        );
        return list.stream().collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));
    }

    private static String maskPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return "";
        }
        String p = phone.trim();
        if (p.length() >= 11) {
            return p.substring(0, 3) + "****" + p.substring(7);
        }
        if (p.length() >= 4) {
            return p.substring(0, 1) + "****" + p.substring(p.length() - 2);
        }
        return "****";
    }

    private static Integer calcAge(LocalDate birthday) {
        if (birthday == null) {
            return null;
        }
        return Period.between(birthday, LocalDate.now()).getYears();
    }

    private PatientBindItemVO toPatientItem(DoctorPatientBind b, Map<Long, DoctorProfile> docByUserId) {
        DoctorProfile dp = docByUserId.get(b.getDoctorUserId());
        return new PatientBindItemVO(
                b.getId(),
                b.getDoctorUserId(),
                dp != null ? dp.getName() : "",
                dp != null ? dp.getDept() : null,
                dp != null ? dp.getTitle() : null,
                dp != null ? dp.getDoctorNo() : null,
                b.getStatus() != null ? b.getStatus() : 0,
                b.getInitiator() != null ? b.getInitiator() : 0,
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }

    private DoctorBindItemVO toDoctorItem(DoctorPatientBind b, Map<Long, PatientProfile> patientByUserId,
                                          Map<Long, SysUser> userById) {
        PatientProfile pp = patientByUserId.get(b.getPatientUserId());
        SysUser u = userById.get(b.getPatientUserId());
        LocalDate birthday = pp != null ? pp.getBirthday() : null;
        return new DoctorBindItemVO(
                b.getId(),
                b.getPatientUserId(),
                pp != null ? pp.getName() : "",
                b.getStatus() != null ? b.getStatus() : 0,
                b.getInitiator() != null ? b.getInitiator() : 0,
                b.getCreatedAt(),
                b.getUpdatedAt(),
                pp != null ? pp.getGender() : null,
                birthday,
                calcAge(birthday),
                maskPhone(u != null ? u.getPhone() : null)
        );
    }

    private AdminBindItemVO toAdminItem(DoctorPatientBind b, Map<Long, DoctorProfile> dmap,
                                        Map<Long, PatientProfile> pmap) {
        DoctorProfile dp = dmap.get(b.getDoctorUserId());
        PatientProfile pp = pmap.get(b.getPatientUserId());
        return new AdminBindItemVO(
                b.getId(),
                b.getDoctorUserId(),
                b.getPatientUserId(),
                dp != null ? dp.getName() : "",
                pp != null ? pp.getName() : "",
                b.getStatus() != null ? b.getStatus() : 0,
                b.getInitiator() != null ? b.getInitiator() : 0,
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }
}
