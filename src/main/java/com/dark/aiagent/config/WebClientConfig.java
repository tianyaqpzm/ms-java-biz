package com.dark.aiagent.config;

import java.time.Duration;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.channel.ChannelOption;
import reactor.netty.http.client.HttpClient;

/**
 * WebClient 配置类：用于调用外部服务（如 ms-py-agent）。 支持负载均衡与 JWT Token 自动透传。
 */
@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder().filter(jwtFilter());
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        // 配置底层 HttpClient 的超时时间，与网关保持一致（默认 5min）
        HttpClient httpClient =
                HttpClient.create().option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                        .responseTimeout(Duration.ofSeconds(300));

        return builder.clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }

    /**
     * JWT 透传过滤器：从 SecurityContext 中获取当前 Token 并注入请求头。
     */
    private ExchangeFilterFunction jwtFilter() {
        return (request, next) -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getCredentials() instanceof String token) {
                ClientRequest filteredRequest = ClientRequest.from(request)
                        .headers(headers -> headers.setBearerAuth(token)).build();
                return next.exchange(filteredRequest);
            }
            return next.exchange(request);
        };
    }
}
