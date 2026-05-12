package com.dark.aiagent.infrastructure.persistence.repository;

import com.dark.aiagent.domain.mcp.McpPlugin;
import com.dark.aiagent.domain.mcp.repository.McpPluginRepository;
import com.dark.aiagent.infrastructure.persistence.entity.McpPluginDO;
import com.dark.aiagent.infrastructure.persistence.mapper.McpPluginMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class McpPluginRepositoryImpl implements McpPluginRepository {

    private final McpPluginMapper mcpPluginMapper;

    @Override
    public List<McpPlugin> findAll() {
        return mcpPluginMapper.selectList(null).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<McpPlugin> findEnabled() {
        return mcpPluginMapper.selectList(null).stream()
                .filter(McpPluginDO::getIsEnabled)
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public McpPlugin findById(UUID id) {
        McpPluginDO pluginDO = mcpPluginMapper.selectById(id);
        return pluginDO != null ? toDomain(pluginDO) : null;
    }

    @Override
    public void save(McpPlugin plugin) {
        McpPluginDO pluginDO = new McpPluginDO();
        pluginDO.setId(plugin.getId());
        pluginDO.setName(plugin.getName());
        pluginDO.setTitle(plugin.getTitle());
        pluginDO.setDescription(plugin.getDescription());
        pluginDO.setIcon(plugin.getIcon());
        pluginDO.setType(plugin.getType());
        pluginDO.setConfig(plugin.getConfig());
        pluginDO.setIsEnabled(plugin.isEnabled());
        pluginDO.setIsSystem(plugin.isSystem());
        pluginDO.setCreateTime(plugin.getCreateTime());
        pluginDO.setUpdateTime(OffsetDateTime.now());
        
        if (plugin.getId() != null && mcpPluginMapper.selectById(plugin.getId()) != null) {
            mcpPluginMapper.updateById(pluginDO);
        } else {
            if (pluginDO.getCreateTime() == null) {
                pluginDO.setCreateTime(OffsetDateTime.now());
            }
            mcpPluginMapper.insert(pluginDO);
        }
    }

    private McpPlugin toDomain(McpPluginDO pluginDO) {
        return McpPlugin.builder()
                .id(pluginDO.getId())
                .name(pluginDO.getName())
                .title(pluginDO.getTitle())
                .description(pluginDO.getDescription())
                .icon(pluginDO.getIcon())
                .type(pluginDO.getType())
                .config(pluginDO.getConfig())
                .enabled(pluginDO.getIsEnabled())
                .system(pluginDO.getIsSystem())
                .createTime(pluginDO.getCreateTime())
                .updateTime(pluginDO.getUpdateTime())
                .build();
    }
}
