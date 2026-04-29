package com.dark.aiagent.domain.chat.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 聊天消息领域实体 (充血模型)
 */
public class ChatMessage {
    private final Long id;
    private final String sessionId;
    private final String role;
    private final String content;
    private final LocalDateTime createdAt;

    public ChatMessage(Long id, String sessionId, String role, String content, LocalDateTime createdAt) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be empty");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("Role cannot be empty");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
        this.id = id;
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public static ChatMessage create(String sessionId, String role, String content) {
        return new ChatMessage(null, sessionId, role, content, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessage that = (ChatMessage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
