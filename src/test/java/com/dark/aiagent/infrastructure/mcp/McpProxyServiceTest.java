package com.dark.aiagent.infrastructure.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;

import com.dark.aiagent.domain.mcp.McpPlugin;
import com.dark.aiagent.domain.mcp.repository.McpPluginRepository;
import com.dark.aiagent.infrastructure.persistence.entity.McpToolCacheDO;
import com.dark.aiagent.infrastructure.persistence.mapper.McpToolCacheMapper;
import com.dark.aiagent.mcp.McpProtocol;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class McpProxyServiceTest {

    @Mock
    private McpToolCacheMapper toolCacheMapper;
    @Mock
    private McpPluginRepository pluginRepository;
    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private WebClient webClient;

    private McpProxyService mcpProxyService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        mcpProxyService = new McpProxyService(toolCacheMapper, pluginRepository, objectMapper, webClientBuilder);
    }

    @Test
    @DisplayName("应当能通过代理调用远程工具")
    void shouldCallToolThroughProxy() {
        // Given
        String toolName = "test_tool";
        UUID serverId = UUID.randomUUID();
        
        McpToolCacheDO cacheDO = new McpToolCacheDO();
        cacheDO.setName(toolName);
        cacheDO.setServerId(serverId);
        cacheDO.setInputSchema(objectMapper.createObjectNode());
        
        when(toolCacheMapper.selectOne(any())).thenReturn(cacheDO);
        
        ObjectNode config = objectMapper.createObjectNode().put("url", "http://remote-mcp/sse");
        McpPlugin plugin = McpPlugin.builder()
                .id(serverId)
                .enabled(true)
                .config(config)
                .build();
        when(pluginRepository.findById(serverId)).thenReturn(plugin);

        // Mock WebClient chain
        WebClient.RequestHeadersUriSpec getSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec getHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        
        when(webClient.mutate()).thenReturn(webClientBuilder);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        when(webClient.get()).thenReturn(getSpec);
        when(getSpec.accept(MediaType.TEXT_EVENT_STREAM)).thenReturn(getHeadersSpec);
        when(getHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        ServerSentEvent<String> endpointEvent = ServerSentEvent.<String>builder()
                .event("endpoint")
                .data("/messages")
                .build();
        when(responseSpec.bodyToFlux(ServerSentEvent.class)).thenReturn(Flux.just(endpointEvent));

        // Mock Post call
        WebClient.RequestBodyUriSpec postSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec postBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec postHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec postResponseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(postSpec);
        when(postSpec.uri(anyString())).thenReturn(postBodySpec);
        when(postBodySpec.bodyValue(any())).thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenReturn(postResponseSpec);
        
        McpProtocol.ToolResult mockResult = new McpProtocol.ToolResult(List.of(new McpProtocol.Content("text", "Hello")), false);
        ObjectNode rpcResponse = objectMapper.createObjectNode();
        rpcResponse.set("result", objectMapper.valueToTree(mockResult));
        
        when(postResponseSpec.bodyToMono(any(Class.class))).thenReturn(Mono.just(rpcResponse));

        // When
        McpProtocol.ToolResult result = mcpProxyService.callTool(toolName, objectMapper.createObjectNode());

        // Then
        assertNotNull(result);
        assertEquals("Hello", result.content().get(0).text());
    }
}
