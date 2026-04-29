package com.dark.aiagent.interfaces.user.controller;

import com.dark.aiagent.application.user.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Tag(name = "用户微服务接口")
@RestController
@RequestMapping("/rest/dark/v1/user")
public class UserController {

    @Operation(summary = "获取当前网关透传登录人信息")
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(
            @RequestHeader(value = "X-User-Id", defaultValue = "") String userId,
            @RequestHeader(value = "X-User-Name", defaultValue = "") String username,
            @RequestHeader(value = "X-User-Avatar", defaultValue = "") String avatar) {
        
        try {
            // Because headers might contain URL encoded non-ascii strings from gateway proxy
            username = URLDecoder.decode(username, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            log.warn("Failed to decode X-User-Name header");
        }

        UserDto user = UserDto.builder()
                .id(userId)
                .name(username)
                .avatar(avatar)
                .build();

        return ResponseEntity.ok(user);
    }
}
