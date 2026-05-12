package com.dark.aiagent.infrastructure.mcp;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dark.aiagent.infrastructure.persistence.entity.McpToolCacheDO;
import com.dark.aiagent.infrastructure.persistence.mapper.McpToolCacheMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class McpSchemaFetcher {

    private final McpToolCacheMapper toolCacheMapper;
    private final WebClient.Builder vanillaWebClientBuilder;
    private final ObjectMapper objectMapper;

    public McpSchemaFetcher(McpToolCacheMapper toolCacheMapper,
            @Qualifier("vanillaWebClientBuilder") WebClient.Builder vanillaWebClientBuilder,
            ObjectMapper objectMapper) {
        this.toolCacheMapper = toolCacheMapper;
        this.vanillaWebClientBuilder = vanillaWebClientBuilder;
        this.objectMapper = objectMapper;
    }

    public void fetchAndCache(UUID serverId, String sseUrl) {
        log.info("Starting asynchronous schema fetch for server {} at {}", serverId, sseUrl);
        WebClient client = vanillaWebClientBuilder.baseUrl(sseUrl).build();

        AtomicReference<String> messageEndpoint = new AtomicReference<>();

        // 使用 Flux 处理 SSE 流，将 POST 请求作为副作用触发，并从 SSE 中监听响应
        client.get()
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .onStatus(status -> status.isError(), response -> 
                    response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new RuntimeException("SSE connection failed: " + body))))
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .timeout(Duration.ofSeconds(60))
                .flatMap(event -> {
                    String eventType = event.event();
                    String data = event.data();

                    if ("endpoint".equals(eventType) && data != null) {
                        String url = resolveUrl(sseUrl, data);
                        messageEndpoint.set(url);
                        log.info("Found endpoint: {}, sending initialize...", url);
                        return sendPostRequest(url, createInitializeRequest()).then(Mono.empty());
                    } 
                    
                    if ("message".equals(eventType) && data != null) {
                        try {
                            JsonNode node = objectMapper.readTree(data);
                            int id = node.path("id").asInt();
                            log.debug("Received SSE message id={}: {}", id, data);

                            if (id == 1) { // 握手响应
                                log.info("Handshake successful, requesting tools list...");
                                return sendPostRequest(messageEndpoint.get(), createListToolsRequest()).then(Mono.empty());
                            } else if (id == 2) { // 工具列表响应
                                JsonNode tools = node.path("result").path("tools");
                                cacheTools(serverId, tools);
                                return Mono.just(true); // 信号：已完成
                            }
                        } catch (Exception e) {
                            log.error("Failed to parse SSE message: {}", e.getMessage());
                        }
                    }
                    return Mono.empty();
                })
                .take(1) // 只要拿到一个 true (完成信号) 就停止
                .subscribe(
                        success -> log.info("Successfully completed schema fetch for server {}", serverId),
                        error -> log.error("Failed to fetch schema for server {}: {}", serverId, error.getMessage())
                );
    }

    private String resolveUrl(String baseUrl, String data) {
        if (data.startsWith("http")) return data;
        String base = baseUrl.substring(0, baseUrl.lastIndexOf("/"));
        return base + (data.startsWith("/") ? "" : "/") + data;
    }

    private Mono<Void> sendPostRequest(String url, Object body) {
        return vanillaWebClientBuilder.build().post()
                .uri(url)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.isError(), response -> 
                    response.bodyToMono(String.class)
                            .flatMap(b -> Mono.error(new RuntimeException("POST failed: " + b))))
                .bodyToMono(String.class)
                .doOnNext(res -> log.debug("POST response (ignored): {}", res))
                .then();
    }

    private McpProtocol.JsonRpcRequest createInitializeRequest() {
        return new McpProtocol.JsonRpcRequest("2.0", "initialize",
                new McpProtocol.InitializeParams(
                        new McpProtocol.Implementation("ms-java-biz", "1.0.0"), "2024-11-05", Map.of()),
                1);
    }

    private McpProtocol.JsonRpcRequest createListToolsRequest() {
        return new McpProtocol.JsonRpcRequest("2.0", "tools/list", Map.of(), 2);
    }


    private void cacheTools(UUID serverId, JsonNode tools) {
        if (!tools.isArray()) {
            log.warn("Expected tools array, got: {}", tools);
            return;
        }

        log.info("Caching {} tools for server {}", tools.size(), serverId);

        // Clear existing cache for this server
        toolCacheMapper.delete(
                new LambdaQueryWrapper<McpToolCacheDO>().eq(McpToolCacheDO::getServerId, serverId));

        for (JsonNode tool : tools) {
            McpToolCacheDO cacheDO = new McpToolCacheDO();
            cacheDO.setId(UUID.randomUUID());
            cacheDO.setServerId(serverId);
            cacheDO.setName(tool.path("name").asText());
            cacheDO.setDescription(tool.path("description").asText());
            cacheDO.setInputSchema(tool.path("inputSchema"));
            cacheDO.setCreateTime(OffsetDateTime.now());
            cacheDO.setUpdateTime(OffsetDateTime.now());
            toolCacheMapper.insert(cacheDO);
        }
    }
}
