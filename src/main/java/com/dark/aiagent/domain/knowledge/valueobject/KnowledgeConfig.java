package com.dark.aiagent.domain.knowledge.valueobject;

import java.util.List;

/**
 * 知识库文档处理配置 (值对象 - 不可变)
 * 采用嵌套结构以解耦不同阶段的配置逻辑
 */
public record KnowledgeConfig(
    ChunkingConfig chunking,
    IndexingConfig indexing,
    RetrievalConfig retrieval,
    GenerationConfig generation,
    EvaluationConfig evaluation
) {
    /**
     * 数据准备配置 (Step 1)
     */
    public record ChunkingConfig(
        Integer chunkSize,
        Integer chunkOverlap,
        List<String> separators
    ) {
        public ChunkingConfig {
            if (chunkSize != null && chunkSize <= 0) {
                throw new IllegalArgumentException("Chunk size must be positive");
            }
            if (chunkOverlap != null && chunkOverlap < 0) {
                throw new IllegalArgumentException("Chunk overlap cannot be negative");
            }
        }
    }

    /**
     * 索引构建配置 (Step 2)
     */
    public record IndexingConfig(
        String embeddingModel,
        String vectorStore
    ) {}

    /**
     * 检索优化配置 (Step 3)
     */
    public record RetrievalConfig(
        Integer topK,
        Double scoreThreshold,
        Boolean enableHybridSearch,
        Double alphaWeight
    ) {}

    /**
     * 生成与集成配置 (Step 4)
     */
    public record GenerationConfig(
        String generationModel,
        Double temperature,
        Integer maxTokens,
        String systemPrompt
    ) {}

    /**
     * 系统评估配置 (Step 5)
     */
    public record EvaluationConfig(
        Boolean enableEvaluation,
        List<String> evaluationMetrics
    ) {}
}
