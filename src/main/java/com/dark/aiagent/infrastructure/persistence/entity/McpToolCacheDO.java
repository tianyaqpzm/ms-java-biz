package com.dark.aiagent.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dark.aiagent.infrastructure.persistence.handler.PostgresJsonbTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@TableName(value = "ms_mcp_tool_cache", autoResultMap = true)
public class McpToolCacheDO {
    @TableId(type = IdType.AUTO)
    private UUID id;

    private UUID serverId;
    private String name;
    private String description;
    
    @TableField(typeHandler = PostgresJsonbTypeHandler.class)
    private JsonNode inputSchema;
    
    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;
}
