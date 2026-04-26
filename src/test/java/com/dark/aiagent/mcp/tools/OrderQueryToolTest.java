package com.dark.aiagent.mcp.tools;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.dark.aiagent.mcp.McpProtocol.ToolResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * OrderQueryTool 单元测试：执行成功、参数缺失
 */
class OrderQueryToolTest {

    private final OrderQueryTool tool = new OrderQueryTool();
    private final ObjectMapper mapper = new ObjectMapper();

    // LT-01: 传 orderId 返回 success ToolResult
    @Test
    @DisplayName("LT-01: 传入 orderId → 返回 success ToolResult")
    void executeShouldReturnSuccessWithOrderId() {
        ObjectNode args = mapper.createObjectNode();
        args.put("orderId", "ORDER-1001");

        ToolResult result = tool.execute(args);

        assertThat(result.isError()).isFalse();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).text()).contains("ORDER-1001");
    }

    // LT-02: 参数缺失 → 返回 error ToolResult
    @Test
    @DisplayName("LT-02: 参数缺失 → 返回 error ToolResult")
    void executeShouldReturnErrorWhenMissingOrderId() {
        ObjectNode args = mapper.createObjectNode();
        // 不传 orderId

        ToolResult result = tool.execute(args);

        assertThat(result.isError()).isTrue();
        assertThat(result.content().get(0).text()).contains("orderId");
    }

    // LT-03: getName/getDescription/getInputSchema 验证
    @Test
    @DisplayName("LT-03: 工具元数据完整性")
    void toolMetadataShouldBeComplete() {
        assertThat(tool.getName()).isEqualTo("query_order");
        assertThat(tool.getDescription()).isNotBlank();
        assertThat(tool.getInputSchema())
                .containsKey("type")
                .containsKey("properties")
                .containsKey("required");
    }
}
