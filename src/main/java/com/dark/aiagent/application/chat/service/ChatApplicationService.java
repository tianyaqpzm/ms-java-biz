package com.dark.aiagent.application.chat.service;

import com.dark.aiagent.application.chat.dto.ChatSessionDto;
import com.dark.aiagent.domain.chat.entity.ChatMessage;
import com.dark.aiagent.domain.chat.repository.ChatRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatApplicationService {

    private final ChatRepository chatRepository;

    public ChatApplicationService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public List<ChatMessage> getChatHistory(String sessionId) {
        return chatRepository.findMessagesBySessionId(sessionId);
    }

    public List<ChatSessionDto> getAllSessions() {
        return chatRepository.findAllSessions().stream()
                .map(s -> new ChatSessionDto(s.getSessionId(), s.getTitle(), s.getLastActiveTime()))
                .collect(Collectors.toList());
    }

    public void deleteSession(String sessionId) {
        chatRepository.deleteSession(sessionId);
    }
}
