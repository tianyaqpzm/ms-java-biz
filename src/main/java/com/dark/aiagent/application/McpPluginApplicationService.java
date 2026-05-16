package com.dark.aiagent.application;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class McpPluginApplicationService {

    private final McpPluginRepository mcpPluginRepository;
    private final UserPreferenceMapper userPreferenceMapper;
    private final ObjectMapper objectMapper;
    private final McpSchemaFetcher mcpSchemaFetcher;

    private static final String PREF_KEY_MCP_ENABLED = "mcp_enabled_plugins";

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("【MCP-Init】Checking enabled plugins for schema refresh at startup...");
        List<McpPlugin> enabledPlugins = mcpPluginRepository.findAll().stream()
                .filter(McpPlugin::isEnabled).filter(p -> p.getType().equalsIgnoreCase("sse"))
                .collect(Collectors.toList());

        for (McpPlugin plugin : enabledPlugins) {
            log.info("【MCP-Init】Triggering schema refresh for plugin: {} ({})", plugin.getName(),
                    plugin.getId());
            refreshPluginSchema(plugin.getId());
        }
    }

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
        List<McpPlugin> allEnabled = mcpPluginRepository.findAll().stream()
                .filter(McpPlugin::isEnabled).collect(Collectors.toList());

        List<String> enabledIds = getEnabledPluginIds(userId);

        log.info("【MCP-Init】User: {}, allEnabled count: {}, enabledIds: {}", userId,
                allEnabled.size(), enabledIds);

        // 过滤逻辑：包含 (用户显式启用的插件) + (所有系统强制启用的插件)
        List<McpPlugin> baseList = allEnabled.stream()
                .filter(plugin -> plugin.isSystem() || enabledIds.isEmpty()
                        || enabledIds.contains(plugin.getId().toString()))
                .collect(Collectors.toList());



        // 处理自发现逻辑：对于已经在列表中的 java-biz 插件，通过 Builder 创建包含动态 URL 的新对象
        return baseList.stream().map(p -> {
            if ("java-biz".equals(p.getName())) {
                ObjectNode newConfig = objectMapper.createObjectNode();
                if (p.getConfig() != null && p.getConfig().isObject()) {
                    newConfig.setAll((ObjectNode) p.getConfig());
                }
                // 动态注入自发现路径
                newConfig.put("url", "/mcp/sse");

                return McpPlugin.builder().id(p.getId()).name(p.getName()).title(p.getTitle())
                        .description(p.getDescription()).icon(p.getIcon()).type(p.getType())
                        .config(newConfig).enabled(p.isEnabled()).system(p.isSystem())
                        .createTime(p.getCreateTime()).updateTime(p.getUpdateTime()).build();
            }
            return p;
        }).collect(Collectors.toList());
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
            plugin = McpPlugin.builder().id(UUID.randomUUID()).name(plugin.getName())
                    .title(plugin.getTitle()).description(plugin.getDescription())
                    .icon(plugin.getIcon()).type(plugin.getType()).config(plugin.getConfig())
                    .enabled(true) // Enable by default for system
                    .system(false).createTime(OffsetDateTime.now()).updateTime(OffsetDateTime.now())
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

        McpPlugin updated = McpPlugin.builder().id(id)
                .name(updateReq.getName() != null ? updateReq.getName() : existing.getName())
                .title(updateReq.getTitle() != null ? updateReq.getTitle() : existing.getTitle())
                .description(updateReq.getDescription() != null ? updateReq.getDescription()
                        : existing.getDescription())
                .icon(updateReq.getIcon() != null ? updateReq.getIcon() : existing.getIcon())
                .type(updateReq.getType() != null ? updateReq.getType() : existing.getType())
                .config(updateReq.getConfig() != null ? updateReq.getConfig()
                        : existing.getConfig())
                .enabled(existing.isEnabled()).system(existing.isSystem())
                .createTime(existing.getCreateTime()).updateTime(OffsetDateTime.now()).build();

        mcpPluginRepository.save(updated);

        // If URL changed, refresh schema
        if (updated.getType().equalsIgnoreCase("sse") && updated.getConfig() != null) {
            String newUrl = updated.getConfig().path("url").asText();
            String oldUrl =
                    existing.getConfig() != null ? existing.getConfig().path("url").asText() : null;
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
