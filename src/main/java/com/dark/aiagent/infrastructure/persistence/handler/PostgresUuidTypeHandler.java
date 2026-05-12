package com.dark.aiagent.infrastructure.persistence.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * PostgreSQL UUID 类型处理器
 * 解决了 java.util.UUID 在 MyBatis-Plus 中与 PostgreSQL UUID 字段匹配的问题
 */
public class PostgresUuidTypeHandler extends BaseTypeHandler<UUID> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType) throws SQLException {
        // 关键：PostgreSQL 需要明确指定为 Types.OTHER 或直接 setObject(UUID)
        ps.setObject(i, parameter, java.sql.Types.OTHER);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        return value instanceof UUID ? (UUID) value : (value != null ? UUID.fromString(value.toString()) : null);
    }

    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Object value = rs.getObject(columnIndex);
        return value instanceof UUID ? (UUID) value : (value != null ? UUID.fromString(value.toString()) : null);
    }

    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Object value = cs.getObject(columnIndex);
        return value instanceof UUID ? (UUID) value : (value != null ? UUID.fromString(value.toString()) : null);
    }
}
