package com.hospital.backend.auth.dto;

public record LoginResponse(String accessToken, long expiresAt) {
}
