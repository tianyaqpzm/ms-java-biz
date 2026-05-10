package com.dark.aiagent.interfaces.prompt.controller;

import com.dark.aiagent.application.prompt.PromptApplicationService;
import com.dark.aiagent.domain.prompt.entity.Prompt;
import com.dark.aiagent.domain.prompt.entity.PromptTemplate;
import com.dark.aiagent.domain.prompt.entity.PromptVersion;
import com.dark.aiagent.interfaces.prompt.dto.CreateVersionRequest;
import com.dark.aiagent.interfaces.prompt.dto.PromptResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Prompt 接口层 (Interfaces Layer)
 */
@RestController
@RequestMapping("/rest/biz/v1/prompts")
@RequiredArgsConstructor
public class PromptController {

    private final PromptApplicationService promptApplicationService;

    /**
     * Runtime: 获取生效状态的 Prompt 详情
     */
    @GetMapping("/{slug}")
    public PromptResponse getActivePrompt(@PathVariable String slug) {
        Prompt prompt = promptApplicationService.getActivePrompt(slug);
        
        return new PromptResponse(
            prompt.getSlug(),
            prompt.getVersionTag(),
            prompt.getContent(),
            prompt.getModelConfig(),
            prompt.getVariables()
        );
    }

    /**
     * Management: 获取所有模板列表
     */
    @GetMapping("/templates")
    public List<PromptTemplate> listTemplates() {
        return promptApplicationService.listTemplates();
    }

    /**
     * Management: 获取指定模板的版本历史
     */
    @GetMapping("/templates/{templateId}/versions")
    public List<PromptVersion> listVersions(@PathVariable Integer templateId) {
        return promptApplicationService.listVersions(templateId);
    }

    /**
     * Management: 保存新的版本
     */
    @PostMapping("/templates/{templateId}/versions")
    public void saveVersion(@PathVariable Integer templateId, @RequestBody CreateVersionRequest request) {
        PromptVersion version = PromptVersion.builder()
                .templateId(templateId)
                .versionTag(request.versionTag())
                .content(request.content())
                .variables(request.variables())
                .modelConfig(request.modelConfig())
                .isActive(false)
                .build();
        
        promptApplicationService.saveVersion(templateId, version);
    }

    /**
     * Management: 激活指定版本
     */
    @PatchMapping("/versions/{versionId}/activate")
    public void activateVersion(@PathVariable Integer versionId) {
        promptApplicationService.activateVersion(versionId);
    }
}
