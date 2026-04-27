package com.dark.aiagent.mcp;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;
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
    // private final ObjectMapper objectMapper;

    // 🔥 核心：自动注入所有实现了 McpTool 接口的 Bean
    // 这就是一个动态的 "ToolRegistry"
    private final Map<String, McpTool> toolRegistry;

    public McpController(ObjectMapper objectMapper, List<McpTool> tools) {
        // this.objectMapper = objectMapper;
        // 将 List 转为 Map，方便按名字查找 (Key=toolName, Value=ToolInstance)
        this.toolRegistry =
                tools.stream().collect(Collectors.toMap(McpTool::getName, tool -> tool));

        log.info("MCP Server 已启动, 加载了 {} 个工具: {}", toolRegistry.size(), toolRegistry.keySet());
    }

    /**
     * SSE 握手端点
     */
    @GetMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        String sessionId = UUID.randomUUID().toString();
        emitters.put(sessionId, emitter);

        // 资源清理回调
        Runnable cleanup = () -> {
            emitters.remove(sessionId);
            log.debug("SSE 连接断开: {}", sessionId);
        };
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        try {
            // 发送 Endpoint 事件
            String endpointUrl = "/mcp/messages?sessionId=" + sessionId;
            emitter.send(SseEmitter.event().name("endpoint").data(endpointUrl));
            log.info("Client 已连接, Session: {}", sessionId);
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 消息处理端点 (JSON-RPC)
     */
    @PostMapping("/messages")
    public void handleMessage(@RequestParam String sessionId, @RequestBody JsonRpcRequest request) {
        SseEmitter emitter = emitters.get(sessionId);
        if (emitter == null) {
            log.warn("Session 失效或不存在: {}", sessionId);
            return;
        }

        try {
            Object result = null;
            log.info("收到指令: {} [{}]", request.method(), sessionId);

            switch (request.method()) {
                case "initialize" -> result = handleInitialize();
                case "tools/list" -> result = handleListTools();
                case "tools/call" -> result = handleCallTool(request);
                case "notifications/initialized" -> {
                    return;
                } // 忽略通知
                default -> throw new IllegalArgumentException("未知的 method: " + request.method());
            }

            // 发送响应
            if (request.id() != null) {
                JsonRpcResponse response = new JsonRpcResponse("2.0", request.id(), result, null);
                emitter.send(SseEmitter.event().name("message").data(response));
            }

        } catch (Exception e) {
            log.error("处理消息失败", e);
            sendError(emitter, request.id(), -32603, "Internal error: " + e.getMessage());
        }
    }

    // --- 内部处理逻辑 ---

    private Map<String, Object> handleInitialize() {
        return Map.of("protocolVersion", "2024-11-05", "capabilities", Map.of("tools", Map.of()),
                "serverInfo", Map.of("name", "ProductionJavaMcp", "version", "2.0"));
    }

    private Map<String, Object> handleListTools() {
        // 动态将所有 Tool 转换为 Protocol 定义
        List<ToolDefinition> definitions = toolRegistry.values().stream()
                .map(t -> new ToolDefinition(t.getName(), t.getDescription(), t.getInputSchema()))
                .toList();
        return Map.of("tools", definitions);
    }

    private ToolResult handleCallTool(JsonRpcRequest request) {
        String name = request.params().get("name").asText();
        // Jackson 的 JsonNode.get("arguments")
        var args = request.params().get("arguments");

        McpTool tool = toolRegistry.get(name);
        if (tool == null) {
            throw new IllegalArgumentException("工具不存在: " + name);
        }

        log.info("执行工具: {}", name);
        return tool.execute(args);
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
}
