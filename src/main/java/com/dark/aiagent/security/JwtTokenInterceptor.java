package com.dark.aiagent.security;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * RestTemplate 拦截器：从 SecurityContext 中获取当前 JWT Token 并透传到下游请求头中。
 */
@Component
public class JwtTokenInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getCredentials() instanceof String token) {
            // 将 Token 注入 Authorization Header
            request.getHeaders().setBearerAuth(token);
        }
        
        return execution.execute(request, body);
    }
}
