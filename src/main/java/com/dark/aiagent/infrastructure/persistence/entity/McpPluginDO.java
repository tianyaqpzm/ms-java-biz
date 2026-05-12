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
@TableName(value = "ms_mcp_server_registry", autoResultMap = true)
public class McpPluginDO {
    @TableId(type = IdType.AUTO)
    private UUID id;

    private String name;

    private String title;

    private String description;

    private String icon;

    private String type;

    @TableField(typeHandler = PostgresJsonbTypeHandler.class)
    private JsonNode config;

    private Boolean isEnabled;

    private Boolean isSystem;

    private OffsetDateTime createTime;

    private OffsetDateTime updateTime;
}
