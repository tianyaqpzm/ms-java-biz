package com.dark.aiagent.application;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dark.aiagent.domain.mcp.McpPlugin;
import com.dark.aiagent.domain.mcp.repository.McpPluginRepository;
import com.dark.aiagent.infrastructure.mcp.McpSchemaFetcher;
import com.dark.aiagent.infrastructure.persistence.entity.UserPreferenceDO;
import com.dark.aiagent.infrastructure.persistence.mapper.UserPreferenceMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class McpPluginApplicationService {

    private final McpPluginRepository mcpPluginRepository;
    private final UserPreferenceMapper userPreferenceMapper;
    private final ObjectMapper objectMapper;
    private final McpSchemaFetcher mcpSchemaFetcher;

    private static final String PREF_KEY_MCP_ENABLED = "mcp_enabled_plugins";

    public List<McpPlugin> listAll(String userId) {
        List<McpPlugin> allPlugins = mcpPluginRepository.findAll();
        List<String> enabledIds = getEnabledPluginIds(userId);

        return allPlugins.stream().map(plugin -> {
            boolean isEnabledForUser = enabledIds.contains(plugin.getId().toString());
            // Since McpPlugin is immutable mostly, if we need to modify state, we build a new one
            // or modify if allowed.
            // But currently enabled is mutable via toggle() or we can just reconstruct it
            if (plugin.isEnabled() != isEnabledForUser) {
                return McpPlugin.builder().id(plugin.getId()).name(plugin.getName())
                        .title(plugin.getTitle()).description(plugin.getDescription())
                        .icon(plugin.getIcon()).type(plugin.getType()).config(plugin.getConfig())
                        .enabled(isEnabledForUser).system(plugin.isSystem())
                        .createTime(plugin.getCreateTime()).updateTime(plugin.getUpdateTime())
                        .build();
            }
            return plugin;
        }).collect(Collectors.toList());
    }

    public List<McpPlugin> listEnabled(String userId) {
        // TODO: [FE014] 暂时绕过 X-User-Id 校验，默认返回所有全局启用的插件。待前端用户偏好功能完善后再恢复。
        return mcpPluginRepository.findAll().stream()
                .filter(McpPlugin::isEnabled)
                .collect(Collectors.toList());
    }

    @Transactional
    public void togglePlugin(String userId, UUID pluginId) {
        McpPlugin plugin = mcpPluginRepository.findById(pluginId);
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin not found: " + pluginId);
        }

        List<String> enabledIds = new ArrayList<>(getEnabledPluginIds(userId));
        String idStr = pluginId.toString();

        if (enabledIds.contains(idStr)) {
            enabledIds.remove(idStr);
        } else {
            enabledIds.add(idStr);
        }

        saveEnabledPluginIds(userId, enabledIds);
    }

    @Transactional
    public McpPlugin registerPlugin(McpPlugin plugin) {
        // Assign ID if missing
        if (plugin.getId() == null) {
            plugin = McpPlugin.builder()
                    .id(UUID.randomUUID())
                    .name(plugin.getName())
                    .title(plugin.getTitle())
                    .description(plugin.getDescription())
                    .icon(plugin.getIcon())
                    .type(plugin.getType())
                    .config(plugin.getConfig())
                    .enabled(true) // Enable by default for system
                    .system(false)
                    .createTime(OffsetDateTime.now())
                    .updateTime(OffsetDateTime.now())
                    .build();
        }
        
        mcpPluginRepository.save(plugin);
        
        // Trigger async schema refresh
        refreshPluginSchema(plugin.getId());
        
        return plugin;
    }

    @Transactional
    public McpPlugin updatePlugin(UUID id, McpPlugin updateReq) {
        McpPlugin existing = mcpPluginRepository.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Plugin not found: " + id);
        }

        McpPlugin updated = McpPlugin.builder()
                .id(id)
                .name(updateReq.getName() != null ? updateReq.getName() : existing.getName())
                .title(updateReq.getTitle() != null ? updateReq.getTitle() : existing.getTitle())
                .description(updateReq.getDescription() != null ? updateReq.getDescription() : existing.getDescription())
                .icon(updateReq.getIcon() != null ? updateReq.getIcon() : existing.getIcon())
                .type(updateReq.getType() != null ? updateReq.getType() : existing.getType())
                .config(updateReq.getConfig() != null ? updateReq.getConfig() : existing.getConfig())
                .enabled(existing.isEnabled())
                .system(existing.isSystem())
                .createTime(existing.getCreateTime())
                .updateTime(OffsetDateTime.now())
                .build();

        mcpPluginRepository.save(updated);

        // If URL changed, refresh schema
        if (updated.getType().equalsIgnoreCase("sse") && updated.getConfig() != null) {
            String newUrl = updated.getConfig().path("url").asText();
            String oldUrl = existing.getConfig() != null ? existing.getConfig().path("url").asText() : null;
            if (newUrl != null && !newUrl.equals(oldUrl)) {
                refreshPluginSchema(id);
            }
        }

        return updated;
    }

    public void refreshPluginSchema(UUID pluginId) {
        McpPlugin plugin = mcpPluginRepository.findById(pluginId);
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin not found: " + pluginId);
        }

        if (plugin.getType().equalsIgnoreCase("sse") && plugin.getConfig() != null) {
            String sseUrl = plugin.getConfig().path("url").asText();
            if (sseUrl != null && !sseUrl.isEmpty()) {
                mcpSchemaFetcher.fetchAndCache(plugin.getId(), sseUrl);
            }
        }
    }

    private List<String> getEnabledPluginIds(String userId) {
        LambdaQueryWrapper<UserPreferenceDO> query = new LambdaQueryWrapper<>();
        query.eq(UserPreferenceDO::getUserId, userId).eq(UserPreferenceDO::getPreferenceKey,
                PREF_KEY_MCP_ENABLED);

        UserPreferenceDO pref = userPreferenceMapper.selectOne(query);
        if (pref != null && pref.getPreferenceValue() != null) {
            try {
                return objectMapper.convertValue(pref.getPreferenceValue(),
                        new TypeReference<List<String>>() {});
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    private void saveEnabledPluginIds(String userId, List<String> enabledIds) {
        LambdaQueryWrapper<UserPreferenceDO> query = new LambdaQueryWrapper<>();
        query.eq(UserPreferenceDO::getUserId, userId).eq(UserPreferenceDO::getPreferenceKey,
                PREF_KEY_MCP_ENABLED);

        UserPreferenceDO pref = userPreferenceMapper.selectOne(query);
        JsonNode jsonNode = objectMapper.valueToTree(enabledIds);

        if (pref == null) {
            pref = new UserPreferenceDO();
            pref.setUserId(userId);
            pref.setPreferenceKey(PREF_KEY_MCP_ENABLED);
            pref.setPreferenceValue(jsonNode);
            pref.setCreateTime(OffsetDateTime.now());
            pref.setUpdateTime(OffsetDateTime.now());
            userPreferenceMapper.insert(pref);
        } else {
            pref.setPreferenceValue(jsonNode);
            pref.setUpdateTime(OffsetDateTime.now());
            userPreferenceMapper.updateById(pref);
        }
    }
}
