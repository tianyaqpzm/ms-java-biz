package com.dark.aiagent.application.prompt;

import com.dark.aiagent.domain.common.exception.BusinessException;
import com.dark.aiagent.domain.prompt.entity.Prompt;
import com.dark.aiagent.domain.prompt.repository.PromptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Prompt 应用服务 (Application Layer)
 */
@Service
@RequiredArgsConstructor
public class PromptApplicationService {

    private final PromptRepository promptRepository;

    /**
     * 获取生效的 Prompt 详情
     */
    public Prompt getActivePrompt(String slug) {
        return promptRepository.findActiveBySlug(slug)
                .orElseThrow(() -> new BusinessException("DEP_0100", "参数异常：" + slug + "不存在"));
    }
}
