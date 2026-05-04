package com.dark.aiagent.domain.prompt.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Prompt 领域实体
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prompt {
    private String slug;
    private String versionTag;
    private String content;
    private Map<String, Object> modelConfig;
    private Map<String, Object> variables;

    /**
     * 校验 Prompt 是否完整
     */
    public boolean isValid() {
        return slug != null && content != null && !content.isBlank();
    }
}
