package com.dark.aiagent.infrastructure.persistence.handler;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 专门适配 PostgreSQL JSONB 类型的 JacksonTypeHandler
 * 解决了 "column is of type jsonb but expression is of type character varying" 的问题
 */
public class PostgresJacksonTypeHandler extends JacksonTypeHandler {

    public PostgresJacksonTypeHandler(Class<?> type) {
        super(type);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        if (ps != null) {
            PGobject pgObject = new PGobject();
            pgObject.setType("jsonb");
            pgObject.setValue(toJson(parameter));
            ps.setObject(i, pgObject);
        }
    }
}
