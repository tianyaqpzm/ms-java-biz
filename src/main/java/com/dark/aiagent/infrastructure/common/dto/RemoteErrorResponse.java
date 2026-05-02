package com.dark.aiagent.infrastructure.common.dto;

/**
 * 通用的远程服务错误响应 DTO。
 * 用于解析其他微服务（如 ms-py-agent）返回的标准化错误 JSON。
 */
public record RemoteErrorResponse(
    String traceId,
    int status,
    String error_code,
    String error_msg,
    String path,
    String timestamp
) {}
