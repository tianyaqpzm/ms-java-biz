package com.dark.aiagent.infrastructure.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;

public class McpProtocol {
    
    public record JsonRpcRequest(
        String jsonrpc,
        String method,
        Object params,
        Object id
    ) {}

    public record JsonRpcResponse(
        String jsonrpc,
        Object id,
        JsonNode result,
        JsonRpcError error
    ) {}

    public record JsonRpcError(
        int code,
        String message,
        JsonNode data
    ) {}

    public record InitializeParams(
        Implementation implementation,
        String protocolVersion,
        Map<String, Object> capabilities
    ) {}

    public record Implementation(
        String name,
        String version
    ) {}
}
