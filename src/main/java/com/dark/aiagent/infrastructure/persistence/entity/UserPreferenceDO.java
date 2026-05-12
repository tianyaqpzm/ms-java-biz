package com.dark.aiagent.infrastructure.persistence.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.dark.aiagent.infrastructure.persistence.handler.PostgresJsonbTypeHandler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
@TableName(value = "ms_user_preference", autoResultMap = true)
public class UserPreferenceDO {
    @TableId(type = IdType.AUTO)
    private UUID id;
    private String userId;
    private String preferenceKey;

    @TableField(typeHandler = PostgresJsonbTypeHandler.class)
    private JsonNode preferenceValue;

    private OffsetDateTime createTime;
    private OffsetDateTime updateTime;
}
