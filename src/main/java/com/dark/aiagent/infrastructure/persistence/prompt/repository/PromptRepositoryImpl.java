package com.dark.aiagent.infrastructure.persistence.prompt.repository;

import com.dark.aiagent.domain.prompt.entity.Prompt;
import com.dark.aiagent.domain.prompt.repository.PromptRepository;
import com.dark.aiagent.infrastructure.persistence.prompt.entity.PromptVersionDO;
import com.dark.aiagent.infrastructure.persistence.prompt.mapper.PromptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Prompt 仓储实现 (Infrastructure Layer)
 */
@Repository
@RequiredArgsConstructor
public class PromptRepositoryImpl implements PromptRepository {

    private final PromptMapper promptMapper;

    @Override
    public Optional<Prompt> findActiveBySlug(String slug) {
        PromptVersionDO versionDO = promptMapper.findActiveBySlug(slug);
        
        if (versionDO == null) {
            return Optional.empty();
        }

        // 将 DO 转换为 Domain Entity
        Prompt prompt = Prompt.builder()
                .slug(slug)
                .versionTag(versionDO.getVersionTag())
                .content(versionDO.getContent())
                .modelConfig(versionDO.getModelConfig())
                .variables(versionDO.getVariables())
                .build();
                
        return Optional.of(prompt);
    }
}
