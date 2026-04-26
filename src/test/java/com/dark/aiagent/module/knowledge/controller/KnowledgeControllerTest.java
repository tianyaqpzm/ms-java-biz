package com.dark.aiagent.module.knowledge.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.dark.aiagent.module.knowledge.entity.KnowledgeTopic;
import com.dark.aiagent.module.knowledge.service.KnowledgeTopicService;
import com.dark.aiagent.module.knowledge.service.KnowledgeDocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 知识库 API 测试：CRUD + 级联逻辑
 * 使用 @SpringBootTest + MockBean 隔离底层数据库
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class KnowledgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KnowledgeTopicService topicService;

    @MockBean
    private KnowledgeDocumentService documentService;

    @Autowired
    private ObjectMapper objectMapper;

    // LK-01: GET /topics -> 返回主题列表
    @Test
    @DisplayName("LK-01: 获取所有主题列表")
    void shouldReturnTopicList() throws Exception {
        KnowledgeTopic topic = new KnowledgeTopic();
        topic.setId("1");
        topic.setName("测试主题");

        when(topicService.list()).thenReturn(List.of(topic));

        mockMvc.perform(get("/rest/dark/v1/knowledge/topics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("测试主题"));
    }

    // LK-02: POST /topics -> 创建主题
    @Test
    @DisplayName("LK-02: 成功创建新主题")
    void shouldCreateTopic() throws Exception {
        KnowledgeTopic topic = new KnowledgeTopic();
        topic.setName("新架构文档");

        mockMvc.perform(post("/rest/dark/v1/knowledge/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topic)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("新架构文档"));

        verify(topicService, times(1)).save(any());
    }

    // LK-03: DELETE /topics/{id} -> 级联清理
    @Test
    @DisplayName("LK-03: 删除主题并验证级联清理调用")
    void shouldDeleteTopicAndCleanupDocuments() throws Exception {
        String topicId = "topic-999";

        mockMvc.perform(delete("/rest/dark/v1/knowledge/topics/" + topicId))
                .andExpect(status().isOk());


        verify(topicService).removeById(topicId);
        // 验证 documentService 也被调用了清理逻辑 (MyBatis-Plus 的 remove 方法)
        verify(documentService).remove(any());
    }
}
