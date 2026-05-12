package com.dark.aiagent.interfaces.prompt.dto;

import java.util.List;
import java.util.Map;

/**
 * 创建新版本请求 DTO
 */
public record CreateVersionRequest(
    String versionTag,
    String content,
    List<String> variables,
    Map<String, Object> modelConfig
) {}
