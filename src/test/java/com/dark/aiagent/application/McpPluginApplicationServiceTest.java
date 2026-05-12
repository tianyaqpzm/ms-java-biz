package com.dark.aiagent.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dark.aiagent.domain.mcp.McpPlugin;
import com.dark.aiagent.domain.mcp.repository.McpPluginRepository;
import com.dark.aiagent.infrastructure.mcp.McpSchemaFetcher;
import com.dark.aiagent.infrastructure.persistence.entity.UserPreferenceDO;
import com.dark.aiagent.infrastructure.persistence.mapper.UserPreferenceMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
class McpPluginApplicationServiceTest {

    @Mock
    private McpPluginRepository mcpPluginRepository;
    @Mock
    private UserPreferenceMapper userPreferenceMapper;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private McpSchemaFetcher mcpSchemaFetcher;

    @InjectMocks
    private McpPluginApplicationService mcpPluginApplicationService;

    private UUID pluginId;
    private McpPlugin mockPlugin;
    private String userId = "user-123";
    private ObjectMapper realMapper = new ObjectMapper();
    private Map<UUID, McpPlugin> pluginStore = new HashMap<>();

    @BeforeEach
    void setUp() {
        pluginId = UUID.randomUUID();
        ObjectNode config = realMapper.createObjectNode().put("url", "http://test-url");
        mockPlugin = McpPlugin.builder()
                .id(pluginId)
                .name("test-plugin")
                .type("sse")
                .config(config)
                .enabled(true)
                .build();
        pluginStore.clear();
        pluginStore.put(pluginId, mockPlugin);
    }

    private void setupMockRepository() {
        when(mcpPluginRepository.findById(any())).thenAnswer(inv -> pluginStore.get((UUID) inv.getArgument(0)));
        doAnswer(inv -> {
            McpPlugin p = inv.getArgument(0);
            pluginStore.put(p.getId(), p);
            return null;
        }).when(mcpPluginRepository).save(any());
    }

    @Test
    @DisplayName("注册插件时应当生成 ID 并触发 Schema 刷新")
    void shouldRegisterPluginAndTriggerRefresh() {
        // Given
        setupMockRepository();
        ObjectNode config = realMapper.createObjectNode().put("url", "http://new-url");
        McpPlugin newPlugin = McpPlugin.builder()
                .name("new-plugin")
                .type("sse")
                .config(config)
                .build();

        // When
        McpPlugin registered = mcpPluginApplicationService.registerPlugin(newPlugin);

        // Then
        assertNotNull(registered.getId());
        verify(mcpPluginRepository).save(any(McpPlugin.class));
        verify(mcpSchemaFetcher).fetchAndCache(eq(registered.getId()), eq("http://new-url"));
    }

    @Test
    @DisplayName("更新插件时若 URL 变更应当重新刷新 Schema")
    void shouldRefreshSchemaWhenUrlChangesOnUpdate() {
        // Given
        setupMockRepository();
        ObjectNode oldConfig = realMapper.createObjectNode().put("url", "http://old");
        ObjectNode newConfig = realMapper.createObjectNode().put("url", "http://new");

        McpPlugin existing = McpPlugin.builder()
                .id(pluginId)
                .type("sse")
                .config(oldConfig)
                .build();
        pluginStore.put(pluginId, existing);
        
        McpPlugin updateReq = McpPlugin.builder()
                .type("sse")
                .config(newConfig)
                .build();

        // When
        mcpPluginApplicationService.updatePlugin(pluginId, updateReq);

        // Then
        verify(mcpPluginRepository).save(any(McpPlugin.class));
        verify(mcpSchemaFetcher).fetchAndCache(eq(pluginId), eq("http://new"));
    }

    @Test
    @DisplayName("Toggle 插件时应当更新用户偏好")
    void shouldUpdateUserPreferenceOnToggle() {
        // Given
        when(mcpPluginRepository.findById(pluginId)).thenReturn(mockPlugin);
        when(userPreferenceMapper.selectOne(any())).thenReturn(null);
        when(objectMapper.valueToTree(any())).thenReturn(realMapper.createObjectNode());

        // When
        mcpPluginApplicationService.togglePlugin(userId, pluginId);

        // Then
        verify(userPreferenceMapper).insert(any(UserPreferenceDO.class));
    }

    @Test
    @DisplayName("列出所有插件时应当正确合并用户禁用状态")
    void shouldMergeUserPreferencesWhenListingAll() throws Exception {
        // Given
        when(mcpPluginRepository.findAll()).thenReturn(List.of(mockPlugin));
        
        UserPreferenceDO pref = new UserPreferenceDO();
        pref.setPreferenceValue(realMapper.createObjectNode());
        when(userPreferenceMapper.selectOne(any())).thenReturn(pref);
        
        // Mocking enabledIds doesn't contain pluginId
        when(objectMapper.convertValue(any(), any(TypeReference.class))).thenReturn(Collections.emptyList());

        // When
        List<McpPlugin> result = mcpPluginApplicationService.listAll(userId);

        // Then
        assertEquals(1, result.size());
        assertTrue(!result.get(0).isEnabled()); // Should be disabled because it's not in enabledIds
    }
}
