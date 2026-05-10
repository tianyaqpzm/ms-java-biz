package com.dark.aiagent.domain.prompt.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Prompt 模板聚合根 (Aggregate Root)
 */
@Getter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@AllArgsConstructor
public class PromptTemplate {
    private Integer id;
    private String slug;
    private String type;
    private String description;
    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;

    /**
     * 该模板下的所有版本
     */
    private List<PromptVersion> versions;

    public static PromptTemplate create(String slug, String type, String description) {
        return PromptTemplate.builder()
                .slug(slug)
                .type(type)
                .description(description)
                .versions(new ArrayList<>())
                .build();
    }

    /**
     * 业务行为：激活指定版本
     */
    public void activateVersion(Integer versionId) {
        if (versions == null || versions.isEmpty()) {
            return;
        }
        versions.forEach(v -> {
            if (v.getId().equals(versionId)) {
                v.activate();
            } else {
                v.deactivate();
            }
        });
    }

    public void setVersions(List<PromptVersion> versions) {
        this.versions = versions;
    }
}
