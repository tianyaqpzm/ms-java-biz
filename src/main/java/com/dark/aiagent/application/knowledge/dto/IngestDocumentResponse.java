package com.dark.aiagent.application.knowledge.dto;

/**
 * Python Agent 返回的分块入库响应
 * 具体字段可根据实际 Python 返回结构进行扩充
 */
public record IngestDocumentResponse(
    String status,
    String message,
    Object data
) {}
