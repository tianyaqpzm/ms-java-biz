package com.dark.aiagent.application.chat.dto;

import java.time.LocalDateTime;

public record ChatSessionDto(
    String sessionId,
    String title,
    LocalDateTime lastActiveTime
) {}
