package com.dark.aiagent.interfaces.prompt.dto;

import java.util.Map;

public record PromptResponse(
    String slug,
    String version,
    String content,
    Map<String, Object> modelConfig,
    Map<String, Object> variables
) {}
