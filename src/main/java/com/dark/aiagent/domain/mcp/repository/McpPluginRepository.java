package com.dark.aiagent.domain.mcp.repository;

import com.dark.aiagent.domain.mcp.McpPlugin;
import java.util.List;
import java.util.UUID;

public interface McpPluginRepository {
    List<McpPlugin> findAll();
    List<McpPlugin> findEnabled();
    McpPlugin findById(UUID id);
    void save(McpPlugin plugin);
}
