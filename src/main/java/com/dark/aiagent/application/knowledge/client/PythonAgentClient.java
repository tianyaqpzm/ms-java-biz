package com.dark.aiagent.application.knowledge.client;

import com.dark.aiagent.application.knowledge.dto.IngestDocumentRequest;
import com.dark.aiagent.application.knowledge.dto.IngestDocumentResponse;

/**
 * 外部 Python Agent 微服务调用接口 (防腐层/端口)
 * 将底层 HTTP 请求与业务服务隔离
 */
public interface PythonAgentClient {

    /**
     * 发起文档分块入库请求
     *
     * @param request 请求参数
     * @return 响应结果
     */
    IngestDocumentResponse ingestDocument(IngestDocumentRequest request);
}
