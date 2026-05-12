package com.dark.aiagent.domain.prompt.repository;

import com.dark.aiagent.domain.prompt.entity.Prompt;
import com.dark.aiagent.domain.prompt.entity.PromptTemplate;
import com.dark.aiagent.domain.prompt.entity.PromptVersion;

import java.util.List;
import java.util.Optional;

/**
 * Prompt 仓储接口
 */
public interface PromptRepository {

    /**
     * 运行时：获取当前生效的 Prompt
     */
    Optional<Prompt> findActiveBySlug(String slug);

    /**
     * 管理端：获取所有模板
     */
    List<PromptTemplate> findAllTemplates();

    /**
     * 管理端：根据 ID 获取模板
     */
    Optional<PromptTemplate> findTemplateById(Integer id);

    /**
     * 管理端：获取模板下的所有版本
     */
    List<PromptVersion> findVersionsByTemplateId(Integer templateId);

    /**
     * 管理端：获取单个版本
     */
    Optional<PromptVersion> findVersionById(Integer versionId);

    /**
     * 管理端：保存/更新模板
     */
    void saveTemplate(PromptTemplate template);

    /**
     * 管理端：保存新版本
     */
    void saveVersion(PromptVersion version);

    /**
     * 管理端：原子化更新版本生效状态
     */
    void updateVersionStatus(Integer templateId, Integer activeVersionId);
}
