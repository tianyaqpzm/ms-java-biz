package com.dark.aiagent.application.prompt;

import com.dark.aiagent.domain.prompt.entity.Prompt;
import com.dark.aiagent.domain.prompt.repository.PromptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromptApplicationServiceTest {

    @Mock
    private PromptRepository promptRepository;

    @InjectMocks
    private PromptApplicationService promptApplicationService;

    private String testSlug = "test-chef";
    private Prompt mockPrompt;

    @BeforeEach
    void setUp() {
        mockPrompt = Prompt.builder()
                .slug(testSlug)
                .content("Test Content")
                .versionTag("v1")
                .build();
    }

    @Test
    @DisplayName("应当能成功获取生效的 Prompt")
    void shouldGetActivePromptSuccessfully() {
        // Given
        when(promptRepository.findActiveBySlug(testSlug)).thenReturn(Optional.of(mockPrompt));

        // When
        Prompt result = promptApplicationService.getActivePrompt(testSlug);

        // Then
        assertNotNull(result);
        assertEquals(testSlug, result.getSlug());
        verify(promptRepository, times(1)).findActiveBySlug(testSlug);
    }

    @Test
    @DisplayName("当 Prompt 不存在时应当抛出异常")
    void shouldThrowExceptionWhenPromptNotFound() {
        // Given
        when(promptRepository.findActiveBySlug(testSlug)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            promptApplicationService.getActivePrompt(testSlug);
        });

        assertTrue(exception.getMessage().contains("not found"));
    }
}
