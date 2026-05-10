package com.dark.aiagent.domain.prompt.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.List;

/**
 * Prompt 版本实体
 */
@Getter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
public class PromptVersion {
    private Integer id;
    private Integer templateId;
    private String versionTag;
    private String content;
    private List<String> variables;
    private Map<String, Object> modelConfig;
    private Boolean isActive;
    private OffsetDateTime createTime;

    protected void activate() {
        this.isActive = true;
    }

    protected void deactivate() {
        this.isActive = false;
    }

    /**
     * 校验版本是否合法
     */
    public boolean isValid() {
        return versionTag != null && !versionTag.isBlank() && content != null && !content.isBlank();
    }
}
