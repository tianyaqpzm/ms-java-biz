package com.dark.aiagent.interfaces.prompt.dto;

import java.util.List;
import java.util.Map;

public record PromptResponse(
    String slug,
    String version,
    String content,
    Map<String, Object> modelConfig,
    List<String> variables
) {}
