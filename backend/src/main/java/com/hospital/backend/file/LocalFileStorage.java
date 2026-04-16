package com.hospital.backend.file;

import com.hospital.backend.config.FileStorageProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class LocalFileStorage {

    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("uuuu/MM");
    private static final Pattern CATEGORY = Pattern.compile("images|files");

    private final Path root;

    public LocalFileStorage(FileStorageProperties properties) throws IOException {
        this.root = Paths.get(properties.getRoot()).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    /**
     * 保存上传文件，返回相对 {@link #root} 的路径（使用 / 分隔）。
     * <p>category 为 {@code images} 时落在 {@code images/年/月/} 下，否则落在 {@code files/年/月/} 下。
     */
    public String store(MultipartFile file, String category) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        String cat = normalizeCategory(category);
        String ym = YearMonth.now().format(YM);
        String ext = safeExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;
        String rel = cat + "/" + ym + "/" + filename;
        Path dest = resolveChecked(rel);
        Files.createDirectories(dest.getParent());
        file.transferTo(dest);
        return rel.replace('\\', '/');
    }

    private static String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return "files";
        }
        String c = category.trim().toLowerCase(Locale.ROOT);
        if (!CATEGORY.matcher(c).matches()) {
            return "files";
        }
        return c;
    }

    public Path resolveChecked(String storageRelPath) {
        if (storageRelPath == null || storageRelPath.isBlank()) {
            throw new IllegalArgumentException("存储路径无效");
        }
        String normalized = storageRelPath.replace('\\', '/');
        if (normalized.startsWith("/") || normalized.contains("..")) {
            throw new IllegalArgumentException("存储路径非法");
        }
        Path p = root.resolve(normalized).normalize();
        if (!p.startsWith(root)) {
            throw new IllegalArgumentException("存储路径非法");
        }
        return p;
    }

    private static String safeExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "";
        }
        String name = originalFilename.trim();
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return "";
        }
        String ext = name.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!ext.matches("[a-z0-9]{1,10}")) {
            return "";
        }
        return "." + ext;
    }
}
