package com.dark.aiagent.application.knowledge.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 请求 ms-py-agent 进行知识库文档分块入库的请求载体
 */
public record IngestDocumentRequest(
    @JsonProperty("file_path") String filePath,
    @JsonProperty("category") String category,
    @JsonProperty("tenant_id") String tenantId,
    @JsonProperty("chunk_size") Integer chunkSize,
    @JsonProperty("chunk_overlap") Integer chunkOverlap
) {}
