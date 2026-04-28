package com.dark.aiagent.module.knowledge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.dark.aiagent.domain.knowledge.entity.KnowledgeTopic;
import com.dark.aiagent.domain.knowledge.repository.KnowledgeTopicRepository;
import com.dark.aiagent.application.knowledge.service.KnowledgeDocumentApplicationService;
import com.dark.aiagent.interfaces.knowledge.controller.KnowledgeController;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

/**
 * 知识库 API 测试：CRUD + 级联逻辑 使用 @WebMvcTest 仅加载 Web 层并通过 MockBean 隔离底层 Service
 */
@WebMvcTest(KnowledgeController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class KnowledgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KnowledgeTopicRepository topicRepository;

    @MockBean
    private KnowledgeDocumentApplicationService documentService;

    @Autowired
    private ObjectMapper objectMapper;

    // LK-01: GET /topics -> 返回主题列表
    @Test
    @DisplayName("LK-01: 获取所有主题列表")
    void shouldReturnTopicList() throws Exception {
        KnowledgeTopic topic = KnowledgeTopic.builder()
                .id("1")
                .name("测试主题")
                .build();

        when(topicRepository.findAll()).thenReturn(List.of(topic));

        mockMvc.perform(get("/rest/dark/v1/knowledge/topics")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("测试主题"));
    }

    // LK-02: POST /topics -> 创建主题
    @Test
    @DisplayName("LK-02: 成功创建新主题")
    void shouldCreateTopic() throws Exception {
        KnowledgeTopic topic = KnowledgeTopic.builder()
                .name("新架构文档")
                .build();

        mockMvc.perform(
                post("/rest/dark/v1/knowledge/topics").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topic)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("新架构文档"));

        verify(topicRepository, times(1)).save(any());
    }

    // LK-03: DELETE /topics/{id} -> 级联清理
    @Test
    @DisplayName("LK-03: 删除主题并验证级联清理调用")
    void shouldDeleteTopicAndCleanupDocuments() throws Exception {
        String topicId = "topic-999";

        mockMvc.perform(delete("/rest/dark/v1/knowledge/topics/" + topicId))
                .andExpect(status().isOk());


        verify(topicRepository).deleteById(topicId);
        verify(documentService).getDocumentsByTopic(topicId);
    }
}
