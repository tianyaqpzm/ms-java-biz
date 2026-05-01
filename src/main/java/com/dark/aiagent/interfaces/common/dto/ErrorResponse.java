package com.dark.aiagent.interfaces.common.dto;

import java.time.Instant;

/**
 * 统一错误响应 DTO，保持与网关格式一致。
 */
public record ErrorResponse(
    String traceId,
    int status,
    String error,
    String message,
    String path,
    String timestamp
) {
    public static ErrorResponse of(String traceId, int status, String error, String message, String path) {
        return new ErrorResponse(
            traceId != null ? traceId : "",
            status,
            error,
            message,
            path,
            Instant.now().toString()
        );
    }
}
