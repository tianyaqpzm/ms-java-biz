package com.dark.aiagent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.dark.aiagent.security.JwtAuthenticationFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final IgnoreWhiteProperties ignoreWhiteProperties;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, IgnoreWhiteProperties ignoreWhiteProperties) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.ignoreWhiteProperties = ignoreWhiteProperties;
        log.info("【SecurityConfig】Initialized with whitelisted URLs: {}", ignoreWhiteProperties.getUrls());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        String[] ignoreUrls = ignoreWhiteProperties.getUrls().toArray(new String[0]);

        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(ignoreUrls).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Return an empty user details manager to prevent Spring Boot from generating a default security password
        return new InMemoryUserDetailsManager();
    }
}
