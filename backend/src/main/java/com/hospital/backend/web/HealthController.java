package com.hospital.backend.web;

import com.hospital.backend.common.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Value("${spring.application.name:hospital-backend}")
    private String applicationName;

    /**
     * 根路径健康检查（便于网关 / 负载均衡 / K8s probe 配置为 GET /health）。
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> rootHealth() {
        return ApiResponse.ok(healthBody());
    }

    /**
     * 与业务 API 前缀一致的健康检查（前端或文档中常用 /api/health）。
     */
    @GetMapping("/api/health")
    public ApiResponse<Map<String, Object>> apiHealth() {
        return ApiResponse.ok(healthBody());
    }

    private Map<String, Object> healthBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("service", applicationName);
        body.put("timestamp", Instant.now().toString());
        return body;
    }

    @GetMapping("/api/secure/ping")
    public ApiResponse<Map<String, String>> securePing() {
        return ApiResponse.ok(Map.of("message", "authenticated"));
    }
}
