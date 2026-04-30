package com.dark.aiagent.infrastructure.persistence.prompt.repository;

import com.dark.aiagent.domain.prompt.entity.Prompt;
import com.dark.aiagent.infrastructure.persistence.prompt.entity.PromptVersionDO;
import com.dark.aiagent.infrastructure.persistence.prompt.mapper.PromptMapper;
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
class PromptRepositoryImplTest {

    @Mock
    private PromptMapper promptMapper;

    @InjectMocks
    private PromptRepositoryImpl promptRepository;

    @Test
    @DisplayName("应当能正确将数据库 DO 转换为领域模型")
    void shouldMapDOToDomainEntity() {
        // Given
        String slug = "test-slug";
        PromptVersionDO versionDO = new PromptVersionDO();
        versionDO.setVersionTag("v1.0");
        versionDO.setContent("Hello {{name}}");
        
        when(promptMapper.findActiveBySlug(slug)).thenReturn(versionDO);

        // When
        Optional<Prompt> result = promptRepository.findActiveBySlug(slug);

        // Then
        assertTrue(result.isPresent());
        assertEquals("v1.0", result.get().getVersionTag());
        assertEquals("Hello {{name}}", result.get().getContent());
    }

    @Test
    @DisplayName("当数据库无记录时应当返回空 Optional")
    void shouldReturnEmptyWhenNotFound() {
        // Given
        when(promptMapper.findActiveBySlug(anyString())).thenReturn(null);

        // When
        Optional<Prompt> result = promptRepository.findActiveBySlug("none");

        // Then
        assertFalse(result.isPresent());
    }
}
