package com.dark.aiagent.interfaces.prompt.controller;

import com.dark.aiagent.application.prompt.PromptApplicationService;
import com.dark.aiagent.config.SecurityConfig;
import com.dark.aiagent.domain.prompt.entity.Prompt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = PromptController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
)
class PromptControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromptApplicationService promptApplicationService;

    @Test
    @DisplayName("GET /prompts/{slug} 应当返回 Prompt 详情")
    void shouldReturnPromptDetails() throws Exception {
        // Given
        String slug = "chef_persona";
        Prompt mockPrompt = Prompt.builder()
                .slug(slug)
                .versionTag("v1.0.0")
                .content("Chef Prompt Content")
                .modelConfig(new HashMap<>())
                .variables(new java.util.ArrayList<>())
                .build();

        when(promptApplicationService.getActivePrompt(slug)).thenReturn(mockPrompt);

        // When & Then
        mockMvc.perform(get("/rest/biz/v1/prompts/{slug}", slug)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value(slug))
                .andExpect(jsonPath("$.content").value("Chef Prompt Content"))
                .andExpect(jsonPath("$.version").value("v1.0.0"));
    }
}
