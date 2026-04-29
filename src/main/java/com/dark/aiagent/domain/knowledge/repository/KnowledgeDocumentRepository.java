package com.dark.aiagent.domain.knowledge.repository;

import com.dark.aiagent.domain.knowledge.entity.KnowledgeDocument;
import java.util.Optional;

/**
 * 知识文档领域仓储接口
 * 倒置依赖：定义在 Domain 层，由 Infrastructure 层实现
 */
public interface KnowledgeDocumentRepository {
    void save(KnowledgeDocument document);
    Optional<KnowledgeDocument> findById(String id);
    void update(KnowledgeDocument document);
    void deleteById(String id);
    java.util.List<KnowledgeDocument> findByTopicId(String topicId);
}
