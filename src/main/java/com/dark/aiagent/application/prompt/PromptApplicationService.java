package com.dark.aiagent.application.prompt;

import com.dark.aiagent.domain.common.exception.BusinessException;
import com.dark.aiagent.domain.prompt.entity.Prompt;
import com.dark.aiagent.domain.prompt.entity.PromptTemplate;
import com.dark.aiagent.domain.prompt.entity.PromptVersion;
import com.dark.aiagent.domain.prompt.repository.PromptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Prompt 应用服务 (Application Layer)
 */
@Service
@RequiredArgsConstructor
public class PromptApplicationService {

    private final PromptRepository promptRepository;

    /**
     * 运行时：获取当前生效的 Prompt 详情
     */
    public Prompt getActivePrompt(String slug) {
        return promptRepository.findActiveBySlug(slug)
                .orElseThrow(() -> new BusinessException("DEP_0100", "提示词不存在：" + slug));
    }

    /**
     * 管理端：获取所有模板列表
     */
    public List<PromptTemplate> listTemplates() {
        return promptRepository.findAllTemplates();
    }

    /**
     * 管理端：获取指定模板的版本历史
     */
    public List<PromptVersion> listVersions(Integer templateId) {
        // 校验模板是否存在
        promptRepository.findTemplateById(templateId)
                .orElseThrow(() -> new BusinessException("DEP_0100", "模板不存在：" + templateId));
        return promptRepository.findVersionsByTemplateId(templateId);
    }

    /**
     * 管理端：保存新版本
     */
    public void saveVersion(Integer templateId, PromptVersion version) {
        // 校验模板
        promptRepository.findTemplateById(templateId)
                .orElseThrow(() -> new BusinessException("DEP_0100", "模板不存在：" + templateId));
        
        // 校验版本号是否重复
        List<PromptVersion> existing = promptRepository.findVersionsByTemplateId(templateId);
        boolean duplicate = existing.stream()
                .anyMatch(v -> v.getVersionTag().equals(version.getVersionTag()));
        if (duplicate) {
            throw new BusinessException("DEP_0101", "版本号已存在：" + version.getVersionTag());
        }

        // 保存
        promptRepository.saveVersion(version);
    }

    /**
     * 管理端：激活指定版本
     */
    public void activateVersion(Integer versionId) {
        PromptVersion version = promptRepository.findVersionById(versionId)
                .orElseThrow(() -> new BusinessException("DEP_0100", "版本不存在：" + versionId));
        
        promptRepository.updateVersionStatus(version.getTemplateId(), versionId);
    }
}
