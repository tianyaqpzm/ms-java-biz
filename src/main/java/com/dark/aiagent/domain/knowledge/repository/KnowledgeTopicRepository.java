package com.dark.aiagent.domain.knowledge.repository;

import com.dark.aiagent.domain.knowledge.entity.KnowledgeTopic;
import java.util.List;
import java.util.Optional;

public interface KnowledgeTopicRepository {
    List<KnowledgeTopic> findAll();
    Optional<KnowledgeTopic> findById(String id);
    void save(KnowledgeTopic topic);
    void deleteById(String id);
}
