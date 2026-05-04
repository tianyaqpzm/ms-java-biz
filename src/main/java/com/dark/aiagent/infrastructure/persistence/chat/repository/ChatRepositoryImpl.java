package com.dark.aiagent.infrastructure.persistence.chat.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dark.aiagent.domain.chat.entity.ChatMessage;
import com.dark.aiagent.domain.chat.entity.ChatSession;
import com.dark.aiagent.domain.chat.repository.ChatRepository;
import com.dark.aiagent.infrastructure.persistence.chat.entity.ChatMessageDO;
import com.dark.aiagent.infrastructure.persistence.chat.entity.ChatSessionDO;
import com.dark.aiagent.infrastructure.persistence.chat.mapper.ChatMessageMapper;
import com.dark.aiagent.infrastructure.persistence.chat.mapper.ChatSessionMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ChatRepositoryImpl implements ChatRepository {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatSessionMapper chatSessionMapper;

    public ChatRepositoryImpl(ChatMessageMapper chatMessageMapper, ChatSessionMapper chatSessionMapper) {
        this.chatMessageMapper = chatMessageMapper;
        this.chatSessionMapper = chatSessionMapper;
    }

    @Override
    public List<ChatMessage> findMessagesBySessionId(String sessionId) {
        List<ChatMessageDO> list = chatMessageMapper.selectList(
            Wrappers.<ChatMessageDO>lambdaQuery()
                    .eq(ChatMessageDO::getSessionId, sessionId)
                    .orderByAsc(ChatMessageDO::getCreatedAt)
        );
        return list.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void saveMessage(ChatMessage message) {
        ChatMessageDO doEntity = toDO(message);
        chatMessageMapper.insert(doEntity);
        // Note: chat_sessions table is automatically updated via database trigger trg_sync_chat_session
    }

    @Override
    public List<ChatSession> findAllSessions() {
        // Now querying directly from the optimized chat_sessions table
        List<ChatSessionDO> doList = chatSessionMapper.selectList(
                Wrappers.<ChatSessionDO>lambdaQuery()
                        .orderByDesc(ChatSessionDO::getLastActiveTime)
        );
        return doList.stream()
                .map(d -> new ChatSession(d.getSessionId(), d.getTitle(), d.getLastActiveTime()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSession(String sessionId) {
        // 1. Delete session metadata
        chatSessionMapper.deleteById(sessionId);
        // 2. Delete all messages in the session
        chatMessageMapper.delete(
            Wrappers.<ChatMessageDO>lambdaQuery()
                    .eq(ChatMessageDO::getSessionId, sessionId)
        );
    }

    private ChatMessage toDomain(ChatMessageDO doEntity) {
        if (doEntity == null) return null;
        return new ChatMessage(
                doEntity.getId(),
                doEntity.getSessionId(),
                doEntity.getRole(),
                doEntity.getContent(),
                doEntity.getCreatedAt()
        );
    }

    private ChatMessageDO toDO(ChatMessage domain) {
        if (domain == null) return null;
        return ChatMessageDO.builder()
                .id(domain.getId())
                .sessionId(domain.getSessionId())
                .role(domain.getRole())
                .content(domain.getContent())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
