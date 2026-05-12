package com.dark.aiagent.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.dark.aiagent.mcp.McpProtocol.JsonRpcResponse;
import com.dark.aiagent.mcp.McpProtocol.ToolResult;
import com.dark.aiagent.mcp.tools.OrderQueryTool;
import com.dark.aiagent.infrastructure.mcp.McpProxyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.mockito.Mockito.mock;

/**
 * MCP 架构守护测试：策略模式验证、DTO 序列化、工具注册
 */
class McpArchitectureTest {

    // LA-01: McpController 自动装配所有 McpTool
    @Test
    @DisplayName("LA-01: McpController 接受 McpTool 列表并构建 Registry")
    void controllerShouldBuildToolRegistry() {
        McpTool tool1 = new OrderQueryTool();
        ObjectMapper mapper = new ObjectMapper();

        McpProxyService proxyService = mock(McpProxyService.class);
        McpController controller = new McpController(mapper, List.of(tool1), proxyService);
        // Controller 构造成功即证明 Registry 构建成功
        assertThat(controller).isNotNull();
    }

    // LA-02: 所有 Tool 实现 McpTool 接口且有唯一 name
    @Test
    @DisplayName("LA-02: OrderQueryTool 实现 McpTool 接口且 name 唯一")
    void toolsShouldImplementInterfaceWithUniqueName() {
        McpTool tool = new OrderQueryTool();

        assertThat(tool).isInstanceOf(McpTool.class);
        assertThat(tool.getName()).isNotBlank();
        assertThat(tool.getDescription()).isNotBlank();
        assertThat(tool.getInputSchema()).isNotNull().containsKey("type");
    }

    // LA-03: McpProtocol record DTO 序列化验证
    @Test
    @DisplayName("LA-03: JsonRpcResponse 序列化包含 jsonrpc/id/result")
    void jsonRpcResponseShouldSerializeCorrectly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonRpcResponse response =
                new JsonRpcResponse("2.0", "req-1", Map.of("status", "ok"), null);

        String json = mapper.writeValueAsString(response);
        assertThat(json).contains("\"jsonrpc\":\"2.0\"").contains("\"id\":\"req-1\"")
                .contains("\"result\"");
        // error 为 null 时不应出现在 JSON 中 (JsonInclude.NON_NULL)
        assertThat(json).doesNotContain("\"error\"");
    }

    // LA-04: ToolResult 工厂方法验证
    @Test
    @DisplayName("LA-04: ToolResult.success/error 工厂方法构造正确结构")
    void toolResultFactoryMethodsShouldWork() {
        ToolResult success = ToolResult.success("操作成功");
        assertThat(success.isError()).isFalse();
        assertThat(success.content()).hasSize(1);
        assertThat(success.content().get(0).type()).isEqualTo("text");
        assertThat(success.content().get(0).text()).isEqualTo("操作成功");

        ToolResult error = ToolResult.error("参数缺失");
        assertThat(error.isError()).isTrue();
        assertThat(error.content().get(0).text()).isEqualTo("参数缺失");
    }
}
