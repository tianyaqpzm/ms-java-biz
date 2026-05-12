-- 1. 重命名原有的 MCP 插件表，以符合新的架构设计
ALTER TABLE ms_mcp_plugin RENAME TO ms_mcp_server_registry;

-- 2. 创建 MCP 工具缓存表 (mcp_tools_cache -> ms_mcp_tool_cache)
CREATE TABLE ms_mcp_tool_cache (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    server_id UUID NOT NULL REFERENCES ms_mcp_server_registry(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    input_schema JSONB NOT NULL,
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(server_id, name)
);

COMMENT ON TABLE ms_mcp_tool_cache IS 'MCP 工具Schema缓存表';
COMMENT ON COLUMN ms_mcp_tool_cache.server_id IS '所属服务端 ID';
COMMENT ON COLUMN ms_mcp_tool_cache.name IS '工具名称';
COMMENT ON COLUMN ms_mcp_tool_cache.input_schema IS '工具参数 Schema';

-- 3. 创建用户偏好表 (user_mcp_preferences -> ms_user_preference)
CREATE TABLE ms_user_preference (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(100) NOT NULL,
    preference_key VARCHAR(100) NOT NULL,
    preference_value JSONB NOT NULL,
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, preference_key)
);

COMMENT ON TABLE ms_user_preference IS '用户偏好配置表';
COMMENT ON COLUMN ms_user_preference.user_id IS '用户ID';
COMMENT ON COLUMN ms_user_preference.preference_key IS '偏好键名 (例如 mcp_enabled_plugins)';
COMMENT ON COLUMN ms_user_preference.preference_value IS '偏好内容 (JSON 数组存储 ID 列表)';
