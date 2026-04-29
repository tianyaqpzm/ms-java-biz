package com.dark.aiagent.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 忽略白名单配置
 */
@Component
@ConfigurationProperties(prefix = "app.security.ignore")
public class IgnoreWhiteProperties {
    /**
     * 放行的URL列表
     */
    private List<String> urls = new ArrayList<>();

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
