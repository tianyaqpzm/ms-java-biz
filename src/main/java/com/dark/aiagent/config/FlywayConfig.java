package com.dark.aiagent.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * Flyway 延迟初始化配置
 * 确保在 Nacos 配置完全加载且应用就绪后执行数据库迁移
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class FlywayConfig {

    private final Environment env;

    @Value("${spring.flyway.url}")
    private String url;

    @Value("${spring.flyway.user}")
    private String user;

    @Value("${spring.flyway.password}")
    private String password;

    @Value("${app.flyway.manual-trigger:true}")
    private boolean enabled;

    @Bean
    public Flyway flyway() {
        return Flyway.configure()
                .dataSource(url, user, password)
                .locations(env.getProperty("spring.flyway.locations", "classpath:db/migration"))
                .baselineOnMigrate(true)
                .validateOnMigrate(false)
                .connectRetries(20)
                // 关键兼容性参数：使用表锁而非咨询锁，规避代理环境下的连接重置问题
                .configuration(Map.of("flyway.postgresql.transactional.lock", "false"))
                .load();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        if (!enabled) {
            log.info("【Flyway】配置为禁用，跳过迁移任务");
            return;
        }
        log.info("【Flyway】检测到应用就绪，开始执行延迟迁移任务...");
        try {
            // 从上下文中获取 Bean 确保单例执行
            event.getApplicationContext().getBean(Flyway.class).migrate();
            log.info("【Flyway】手动迁移任务执行成功");
        } catch (Exception e) {
            log.error("【Flyway】手动迁移任务执行失败: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
