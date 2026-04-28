package com.dark.aiagent.application.knowledge.service;

import com.dark.aiagent.domain.knowledge.entity.KnowledgeDocument;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * 知识文档应用服务接口 (Application Layer)
 * 编排领域逻辑与基础设施，剥离框架依赖
 */
public interface KnowledgeDocumentApplicationService {
    KnowledgeDocument uploadDocument(String topicId, MultipartFile file);
    Resource getDocumentResource(String documentId);
    void deleteDocument(String documentId);
    Map<String, Object> initiateIngest(String documentId, Map<String, Object> ingestPayload);
    java.util.List<KnowledgeDocument> getDocumentsByTopic(String topicId);
}
