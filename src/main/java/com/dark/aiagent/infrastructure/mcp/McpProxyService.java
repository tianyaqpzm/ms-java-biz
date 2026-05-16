package com.dark.aiagent.infrastructure.mcp;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dark.aiagent.domain.mcp.McpPlugin;
import com.dark.aiagent.domain.mcp.repository.McpPluginRepository;
import com.dark.aiagent.infrastructure.persistence.entity.McpToolCacheDO;
import com.dark.aiagent.infrastructure.persistence.mapper.McpToolCacheMapper;
import com.dark.aiagent.mcp.McpProtocol;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class McpProxyService {

    private final McpToolCacheMapper toolCacheMapper;
    private final McpPluginRepository pluginRepository;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder vanillaWebClientBuilder;

    @org.springframework.beans.factory.annotation.Value("${server.port:8080}")
    private int serverPort;

    public McpProxyService(McpToolCacheMapper toolCacheMapper, McpPluginRepository pluginRepository,
            ObjectMapper objectMapper,
            @Qualifier("vanillaWebClientBuilder") WebClient.Builder webClientBuilder) {
        this.toolCacheMapper = toolCacheMapper;
        this.pluginRepository = pluginRepository;
        this.objectMapper = objectMapper;
        this.vanillaWebClientBuilder = webClientBuilder;
    }

    public List<McpProtocol.ToolDefinition> getDynamicTools() {
        return toolCacheMapper.selectList(new LambdaQueryWrapper<>()).stream()
                .map(this::toDefinition).toList();
    }

    @SuppressWarnings("unchecked")
    private McpProtocol.ToolDefinition toDefinition(McpToolCacheDO cacheDO) {
        return new McpProtocol.ToolDefinition(cacheDO.getName(), cacheDO.getDescription(),
                objectMapper.convertValue(cacheDO.getInputSchema(), Map.class));
    }

    public McpProtocol.ToolResult callTool(String toolName, JsonNode arguments) {
        log.info("Proxying tool call: {}", toolName);

        McpToolCacheDO toolCache = toolCacheMapper.selectOne(
                new LambdaQueryWrapper<McpToolCacheDO>().eq(McpToolCacheDO::getName, toolName));

        if (toolCache == null) {
            return McpProtocol.ToolResult.error("Tool not found in cache: " + toolName);
        }

        McpPlugin plugin = pluginRepository.findById(toolCache.getServerId());
        if (plugin == null || !plugin.isEnabled()) {
            return McpProtocol.ToolResult
                    .error("Plugin not found or disabled for tool: " + toolName);
        }
        String sseUrl = plugin.getConfig().path("url").asText();

        try {
            return executeRemoteCall(sseUrl, toolName, arguments).block(Duration.ofSeconds(30));
        } catch (Exception e) {
            log.error("Failed to proxy tool call to {}", sseUrl, e);
            return McpProtocol.ToolResult.error("Remote call failed: " + e.getMessage());
        }
    }

    private Mono<McpProtocol.ToolResult> executeRemoteCall(String sseUrl, String toolName,
            JsonNode arguments) {
        String finalSseUrl = sseUrl;
        if (sseUrl.startsWith("/")) {
            finalSseUrl = "http://localhost:" + serverPort + sseUrl;
        }
        
        final String effectiveSseUrl = finalSseUrl;
        WebClient client = vanillaWebClientBuilder.clone().baseUrl(effectiveSseUrl).build();
        AtomicReference<String> messageEndpoint = new AtomicReference<>();

        return client.get().accept(MediaType.TEXT_EVENT_STREAM).retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {}).timeout(Duration.ofSeconds(10))
                .takeUntil(event -> "endpoint".equals(event.event())).doOnNext(event -> {
                    if ("endpoint".equals(event.event()) && event.data() != null) {
                        String url = event.data().toString();
                        if (!url.startsWith("http")) {
                            try {
                                java.net.URI baseUri = new java.net.URI(effectiveSseUrl);
                                url = baseUri.resolve(url).toString();
                            } catch (Exception e) {
                                log.error("Failed to resolve endpoint URL: {}", url, e);
                            }
                        }
                        messageEndpoint.set(url);
                    }
                }).then(Mono.defer(() -> {
                    if (messageEndpoint.get() == null) {
                        return Mono.error(new RuntimeException("Could not find message endpoint"));
                    }

                    McpProtocol.JsonRpcRequest callReq =
                            new McpProtocol.JsonRpcRequest("2.0", UUID.randomUUID().toString(),
                                    "tools/call", objectMapper.createObjectNode()
                                            .put("name", toolName).set("arguments", arguments));

                    return vanillaWebClientBuilder.clone().build().post().uri(messageEndpoint.get())
                            .bodyValue(callReq).retrieve().bodyToMono(JsonNode.class).map(res -> {
                                JsonNode resultNode = res.path("result");
                                return objectMapper.convertValue(resultNode,
                                        McpProtocol.ToolResult.class);
                            });
                }));
    }
}
