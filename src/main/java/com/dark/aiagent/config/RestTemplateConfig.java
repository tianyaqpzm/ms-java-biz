package com.dark.aiagent.config;

import com.dark.aiagent.security.JwtTokenInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    private final JwtTokenInterceptor jwtTokenInterceptor;

    public RestTemplateConfig(JwtTokenInterceptor jwtTokenInterceptor) {
        this.jwtTokenInterceptor = jwtTokenInterceptor;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(jwtTokenInterceptor);
        return restTemplate;
    }
}
