package com.dark.aiagent.domain.knowledge.valueobject;

import java.util.List;

/**
 * 知识库文档处理配置 (值对象 - 不可变)
 */
public record KnowledgeConfig(
    Integer chunkSize,
    Integer chunkOverlap,
    List<String> separators,
    String embeddingModel,
    String vectorStore,
    Integer topK,
    Double scoreThreshold,
    Boolean enableHybridSearch,
    Double alphaWeight
) {
    public KnowledgeConfig {
        if (chunkSize != null && chunkSize <= 0) {
            throw new IllegalArgumentException("Chunk size must be positive");
        }
        if (chunkOverlap != null && chunkOverlap < 0) {
            throw new IllegalArgumentException("Chunk overlap cannot be negative");
        }
    }
}
