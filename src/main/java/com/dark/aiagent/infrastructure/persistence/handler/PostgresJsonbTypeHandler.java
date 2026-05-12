package com.dark.aiagent.infrastructure.persistence.handler;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import com.fasterxml.jackson.databind.JsonNode;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * PostgreSQL JSONB 类型处理器
 * 继承自 JacksonTypeHandler，并在设置参数时显式指定 Types.OTHER 以适配 PostgreSQL 的 JSONB 字段
 */
@MappedTypes(JsonNode.class)
public class PostgresJsonbTypeHandler extends JacksonTypeHandler {

    public PostgresJsonbTypeHandler(Class<?> type) {
        super(type);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        // 关键：PostgreSQL 的 JSONB 字段不接受 VARCHAR，必须指定为 OTHER
        ps.setObject(i, toJson(parameter), java.sql.Types.OTHER);
    }
}
