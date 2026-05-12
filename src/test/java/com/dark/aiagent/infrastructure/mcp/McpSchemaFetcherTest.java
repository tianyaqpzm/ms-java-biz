package com.dark.aiagent.infrastructure.mcp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;

import com.dark.aiagent.infrastructure.persistence.mapper.McpToolCacheMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class McpSchemaFetcherTest {

    @Mock
    private McpToolCacheMapper toolCacheMapper;
    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private WebClient webClient;

    private McpSchemaFetcher mcpSchemaFetcher;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        mcpSchemaFetcher = new McpSchemaFetcher(toolCacheMapper, webClientBuilder, objectMapper);
    }

    @Test
    @DisplayName("应当能正确处理 SSE 流并缓存工具列表")
    void shouldFetchAndCacheTools() throws Exception {
        // Given
        UUID serverId = UUID.randomUUID();
        String sseUrl = "http://mcp-server/sse";

        // Mock SSE request
        WebClient.RequestHeadersUriSpec getSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec getHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(getSpec);
        when(getSpec.accept(MediaType.TEXT_EVENT_STREAM)).thenReturn(getHeadersSpec);
        when(getHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        // Events: endpoint -> message(id=1) -> message(id=2)
        ServerSentEvent<String> endpointEvent = ServerSentEvent.<String>builder()
                .event("endpoint")
                .data("/messages")
                .build();
        
        ObjectNode handshakeResponse = objectMapper.createObjectNode();
        handshakeResponse.put("id", 1);
        ServerSentEvent<String> handshakeEvent = ServerSentEvent.<String>builder()
                .event("message")
                .data(handshakeResponse.toString())
                .build();

        ObjectNode toolsResponse = objectMapper.createObjectNode();
        toolsResponse.put("id", 2);
        ObjectNode result = toolsResponse.putObject("result");
        result.putArray("tools").addObject()
                .put("name", "test_tool")
                .put("description", "A test tool");
        ServerSentEvent<String> toolsEvent = ServerSentEvent.<String>builder()
                .event("message")
                .data(toolsResponse.toString())
                .build();

        when(responseSpec.bodyToFlux(any(ParameterizedTypeReference.class)))
                .thenReturn(Flux.just(endpointEvent, handshakeEvent, toolsEvent));

        // Mock POST requests
        WebClient.RequestBodyUriSpec postSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec postBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec postHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec postResponseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString())).thenReturn(postBodySpec);
        when(postBodySpec.bodyValue(any())).thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenReturn(postResponseSpec);
        when(postResponseSpec.onStatus(any(), any())).thenReturn(postResponseSpec);
        when(postResponseSpec.bodyToMono(String.class)).thenReturn(Mono.just("{}"));

        // When
        mcpSchemaFetcher.fetchAndCache(serverId, sseUrl);

        // Then
        // Wait a bit for async subscription if needed, but since we are using Flux.just, it might be synchronous enough
        // However, subscribe() is called on a Flux, so we might need a small delay or use a countDownLatch
        // For unit test, verify toolCacheMapper interactions
        Thread.sleep(500); // Simple wait for async subscription to complete
        
        verify(toolCacheMapper, atLeastOnce()).delete(any());
        verify(toolCacheMapper, atLeastOnce()).insert(any(com.dark.aiagent.infrastructure.persistence.entity.McpToolCacheDO.class));
    }
}
