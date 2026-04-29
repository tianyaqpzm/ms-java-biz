package com.dark.aiagent.domain.chat.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 聊天会话领域聚合实体
 */
public class ChatSession {
    private final String sessionId;
    private final String title;
    private final LocalDateTime lastActiveTime;

    public ChatSession(String sessionId, String title, LocalDateTime lastActiveTime) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be empty");
        }
        this.sessionId = sessionId;
        this.title = title != null ? title : "New Conversation";
        this.lastActiveTime = lastActiveTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getLastActiveTime() {
        return lastActiveTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatSession that = (ChatSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }
}
