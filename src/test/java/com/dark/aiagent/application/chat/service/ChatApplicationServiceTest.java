package com.dark.aiagent.application.chat.service;

import com.dark.aiagent.domain.chat.repository.ChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class ChatApplicationServiceTest {

    private ChatApplicationService chatApplicationService;

    @Mock
    private ChatRepository chatRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatApplicationService = new ChatApplicationService(chatRepository);
    }

    @Test
    void deleteSession_ShouldInvokeRepository() {
        String sessionId = "test-session-id";
        
        chatApplicationService.deleteSession(sessionId);
        
        verify(chatRepository, times(1)).deleteSession(sessionId);
    }
}
