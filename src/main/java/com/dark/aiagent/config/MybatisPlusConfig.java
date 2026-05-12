package com.dark.aiagent.config;

import java.util.UUID;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.dark.aiagent.infrastructure.persistence.handler.PostgresUuidTypeHandler;

/**
 * MyBatis-Plus 全局配置 解决了 java.util.UUID 类型在 autoResultMap = true 时无法自动识别 TypeHandler 的问题
 */
@Configuration
@MapperScan(
    basePackages = "com.dark.aiagent.infrastructure.persistence.**.mapper",
    annotationClass = org.apache.ibatis.annotations.Mapper.class
)
public class MybatisPlusConfig {

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            // 注册全局 UUID 类型处理器 (自定义，针对 PostgreSQL 优化)
            configuration.getTypeHandlerRegistry().register(UUID.class,
                    PostgresUuidTypeHandler.class);
        };
    }
}
