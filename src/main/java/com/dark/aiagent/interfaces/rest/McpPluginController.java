package com.dark.aiagent.interfaces.rest;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dark.aiagent.application.McpPluginApplicationService;
import com.dark.aiagent.domain.mcp.McpPlugin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rest/biz/v1/mcp-plugins")
@RequiredArgsConstructor
@Slf4j
public class McpPluginController {

    private final McpPluginApplicationService mcpPluginApplicationService;

    @GetMapping
    public List<McpPlugin> listAll(@RequestHeader(value = "X-User-Id", defaultValue = "") String userId) {
        return mcpPluginApplicationService.listAll(userId);
    }

    @GetMapping("/enabled")
    public List<McpPlugin> listEnabled(@RequestHeader(value = "X-User-Id", defaultValue = "") String userId,
                                       @RequestHeader(value = "X-Trace-Id", required = false) String traceId) {
        String logId = (traceId != null && !traceId.isEmpty()) ? traceId : "SYS-" + (System.currentTimeMillis() % 100000);
        log.info("📥 [MCP-Server-{}] Receiving request for enabled plugins. User: {}", logId, userId);
        
        try {
            List<McpPlugin> result = mcpPluginApplicationService.listEnabled(userId);
            log.info("📤 [MCP-Server-{}] Returning {} enabled plugin(s).", logId, result.size());
            return result;
        } catch (Exception e) {
            log.error("❌ [MCP-Server-{}] Failed to list plugins: {}", logId, e.getMessage());
            throw e;
        }
    }

    @PatchMapping("/{id}/toggle")
    public void toggle(@PathVariable UUID id, @RequestHeader(value = "X-User-Id", defaultValue = "") String userId) {
        mcpPluginApplicationService.togglePlugin(userId, id);
    }

    @PostMapping
    public McpPlugin register(@RequestBody McpPlugin plugin) {
        return mcpPluginApplicationService.registerPlugin(plugin);
    }

    @PostMapping("/{id}/refresh")
    public void refresh(@PathVariable UUID id) {
        mcpPluginApplicationService.refreshPluginSchema(id);
    }

    @PutMapping("/{id}")
    public McpPlugin update(@PathVariable UUID id, @RequestBody McpPlugin plugin) {
        return mcpPluginApplicationService.updatePlugin(id, plugin);
    }
}
