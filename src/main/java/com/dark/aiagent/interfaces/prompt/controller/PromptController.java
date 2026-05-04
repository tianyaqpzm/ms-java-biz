package com.dark.aiagent.interfaces.prompt.controller;

import com.dark.aiagent.application.prompt.PromptApplicationService;
import com.dark.aiagent.domain.prompt.entity.Prompt;
import com.dark.aiagent.interfaces.prompt.dto.PromptResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Prompt 接口层 (Interfaces Layer)
 */
@RestController
@RequestMapping("/rest/biz/v1/prompts")
@RequiredArgsConstructor
public class PromptController {

    private final PromptApplicationService promptApplicationService;

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
}
