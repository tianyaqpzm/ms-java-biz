-- MCP 插件/服务端定义表
CREATE TABLE ms_mcp_plugin (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    icon VARCHAR(50),
    type VARCHAR(20) NOT NULL, -- sse, stdio
    config JSONB NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE ms_mcp_plugin IS 'MCP 插件/服务端定义表';
COMMENT ON COLUMN ms_mcp_plugin.name IS '唯一标识符';
COMMENT ON COLUMN ms_mcp_plugin.title IS '显示名称';
COMMENT ON COLUMN ms_mcp_plugin.type IS '连接类型: sse 或 stdio';
COMMENT ON COLUMN ms_mcp_plugin.config IS '连接配置 (URL, Command, Args 等)';
COMMENT ON COLUMN ms_mcp_plugin.is_enabled IS '是否启用';

-- 插入默认数据
INSERT INTO ms_mcp_plugin (name, title, description, icon, type, config, is_enabled, is_system)
VALUES 
('filesystem', '本地文件系统', '允许 AI 访问和操作本地工作目录下的文件', 'folder_open', 'stdio', 
 '{"command": "npx", "args": ["-y", "@modelcontextprotocol/server-filesystem", "/Users/pei/projects"]}', true, true),
('java-biz', '业务能力中心', '提供订单查询、知识库检索等企业核心业务能力', 'business_center', 'sse', 
 '{"url": "/mcp/sse", "messages_url": "/mcp/messages"}', true, true);
