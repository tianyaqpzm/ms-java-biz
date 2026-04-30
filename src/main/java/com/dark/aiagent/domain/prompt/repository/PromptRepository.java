package com.dark.aiagent.domain.prompt.repository;

import com.dark.aiagent.domain.prompt.entity.Prompt;
import java.util.Optional;

/**
 * Prompt 领域仓储接口
 */
public interface PromptRepository {
    
    /**
     * 根据 Slug 获取当前生效的 Prompt
     */
    Optional<Prompt> findActiveBySlug(String slug);
}
