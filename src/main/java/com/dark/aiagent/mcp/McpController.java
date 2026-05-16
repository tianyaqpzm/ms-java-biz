package com.dark.aiagent.mcp;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.dark.aiagent.mcp.McpProtocol.JsonRpcRequest;
import com.dark.aiagent.mcp.McpProtocol.JsonRpcResponse;
import com.dark.aiagent.mcp.McpProtocol.ToolDefinition;
import com.dark.aiagent.mcp.McpProtocol.ToolResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/mcp")
public class McpController {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final Map<String, McpTool> toolRegistry;
    private final com.dark.aiagent.infrastructure.mcp.McpProxyService mcpProxyService;

    public McpController(ObjectMapper objectMapper, List<McpTool> tools,
            com.dark.aiagent.infrastructure.mcp.McpProxyService mcpProxyService) {
        this.mcpProxyService = mcpProxyService;
        this.toolRegistry =
                tools.stream().collect(Collectors.toMap(McpTool::getName, tool -> tool));

        log.info("MCP Server 已启动, 加载了 {} 个静态工具: {}", toolRegistry.size(), toolRegistry.keySet());
    }

    @GetMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        String sessionId = UUID.randomUUID().toString();
        emitters.put(sessionId, emitter);

        Runnable cleanup = () -> {
            emitters.remove(sessionId);
            log.debug("SSE 连接断开: {}", sessionId);
        };
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        try {
            String endpointUrl = "/mcp/messages?sessionId=" + sessionId;
            emitter.send(SseEmitter.event().name("endpoint").data(endpointUrl));
            log.info("Client 已连接, Session: {}", sessionId);
        } catch (IOException e) {
            log.warn("发送初始 endpoint 失败 (Session: {}): {}", sessionId, e.getMessage());
            emitter.complete();
            emitters.remove(sessionId);
        }

        return emitter;
    }

    @PostMapping("/messages")
    public Object handleMessage(@RequestParam(required = false) String sessionId,
            @RequestBody JsonRpcRequest request) {
        SseEmitter emitter = (sessionId != null) ? emitters.get(sessionId) : null;

        try {
            Object result = null;
            log.info("收到指令: {} [Session: {}]", request.method(), sessionId);

            switch (request.method()) {
                case "initialize" -> result = handleInitialize();
                case "tools/list" -> result = handleListTools();
                case "tools/call" -> result = handleCallTool(request);
                case "notifications/initialized" -> {
                    return null;
                }
                default -> throw new IllegalArgumentException("未知的 method: " + request.method());
            }

            if (request.id() != null) {
                JsonRpcResponse response = new JsonRpcResponse("2.0", request.id(), result, null);
                if (emitter != null) {
                    emitter.send(SseEmitter.event().name("message").data(response));
                    return null;
                }
                return response;
            }

        } catch (Exception e) {
            log.error("处理消息失败", e);
            if (emitter != null) {
                sendError(emitter, request.id(), -32603, "Internal error: " + e.getMessage());
            } else {
                return new JsonRpcResponse("2.0", request.id(), null,
                        Map.of("code", -32603, "message", "Internal error: " + e.getMessage()));
            }
        }
        return null;
    }

    private Map<String, Object> handleInitialize() {
        return Map.of("protocolVersion", "2024-11-05", "capabilities", Map.of("tools", Map.of()),
                "serverInfo", Map.of("name", "ProductionJavaMcp", "version", "2.0"));
    }

    private Map<String, Object> handleListTools() {
        List<ToolDefinition> definitions = new java.util.ArrayList<>(toolRegistry.values().stream()
                .map(t -> new ToolDefinition(t.getName(), t.getDescription(), t.getInputSchema()))
                .toList());

        // 直接连接模式：Java 端只返回自己原生的业务工具，插件工具由 Agent 直接连接，不再通过 Java 代理。
        // definitions.addAll(mcpProxyService.getDynamicTools());
        return Map.of("tools", definitions);
    }

    private ToolResult handleCallTool(JsonRpcRequest request) {
        String name = request.params().get("name").asText();
        var args = request.params().get("arguments");

        McpTool tool = toolRegistry.get(name);
        if (tool != null) {
            log.info("执行静态工具: {}", name);
            return tool.execute(args);
        }

        log.info("尝试通过代理执行工具: {}", name);
        return mcpProxyService.callTool(name, args);
    }

    private void sendError(SseEmitter emitter, String id, int code, String message) {
        if (id == null)
            return;
        try {
            Map<String, Object> errorObj = Map.of("code", code, "message", message);
            JsonRpcResponse response = new JsonRpcResponse("2.0", id, null, errorObj);
            emitter.send(SseEmitter.event().name("message").data(response));
        } catch (IOException ex) {
            // ignore
        }
    }

    /**
     * 每 10 秒发送一次心跳，维持 SSE 连接活跃。
     */
    @Scheduled(fixedRate = 10000)
    public void sendHeartbeat() {
        if (emitters.isEmpty()) {
            return;
        }

        log.debug("发送 MCP SSE 心跳, 当前连接数: {}", emitters.size());
        emitters.forEach((sessionId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
            } catch (Exception e) {
                log.warn("清理失效的 SSE 连接: {}", sessionId);
                emitters.remove(sessionId);
            }
        });
    }
}
