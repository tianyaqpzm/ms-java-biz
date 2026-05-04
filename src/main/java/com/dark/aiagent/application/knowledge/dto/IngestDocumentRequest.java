package com.dark.aiagent.application.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 请求 ms-py-agent 进行知识库文档分块入库的请求载体
 */
public record IngestDocumentRequest(
    @JsonProperty("file_path") String filePath,
    @JsonProperty("category") String category,
    @JsonProperty("tenant_id") String tenantId,
    @JsonProperty("chunk_size") Integer chunkSize,
    @JsonProperty("chunk_overlap") Integer chunkOverlap,
    @JsonProperty("separators") List<String> separators,
    @JsonProperty("embedding_model") String embeddingModel,
    @JsonProperty("vector_store") String vectorStore,
    @JsonProperty("top_k") Integer topK,
    @JsonProperty("score_threshold") Double scoreThreshold,
    @JsonProperty("enable_hybrid_search") Boolean enableHybridSearch,
    @JsonProperty("alpha_weight") Double alphaWeight,
    @JsonProperty("generation_model") String generationModel,
    @JsonProperty("temperature") Double temperature,
    @JsonProperty("max_tokens") Integer maxTokens,
    @JsonProperty("system_prompt") String systemPrompt,
    @JsonProperty("enable_evaluation") Boolean enableEvaluation,
    @JsonProperty("evaluation_metrics") List<String> evaluationMetrics
) {}
