package com.dark.aiagent.module.knowledge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dark.aiagent.module.knowledge.entity.KnowledgeDocument;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import java.util.Map;

public interface KnowledgeDocumentService extends IService<KnowledgeDocument> {
    KnowledgeDocument uploadDocument(String topicId, MultipartFile file);
    Resource getDocumentResource(String documentId);
    void deleteDocument(String documentId);
    Map<String, Object> initiateIngest(String documentId, Map<String, Object> ingestPayload);
}
