package com.hospital.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConfigurationProperties(prefix = "app.storage")
public class FileStorageProperties {

    /**
     * 本地附件根目录（相对路径则相对 user.dir）；未配置时默认 {@code upload}，即 backend/upload。
     */
    private String root;

    public String getRoot() {
        if (!StringUtils.hasText(root)) {
            return "upload";
        }
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }
}
