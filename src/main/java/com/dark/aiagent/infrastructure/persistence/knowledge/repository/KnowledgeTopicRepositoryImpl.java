package com.dark.aiagent.infrastructure.persistence.knowledge.repository;

import com.dark.aiagent.domain.knowledge.entity.KnowledgeTopic;
import com.dark.aiagent.domain.knowledge.repository.KnowledgeTopicRepository;
import com.dark.aiagent.infrastructure.persistence.knowledge.entity.KnowledgeTopicDO;
import com.dark.aiagent.infrastructure.persistence.knowledge.mapper.KnowledgeTopicMapper;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class KnowledgeTopicRepositoryImpl implements KnowledgeTopicRepository {

    private final KnowledgeTopicMapper mapper;

    public KnowledgeTopicRepositoryImpl(KnowledgeTopicMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<KnowledgeTopic> findAll() {
        return mapper.selectList(null).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<KnowledgeTopic> findById(String id) {
        return Optional.ofNullable(mapper.selectById(id)).map(this::toDomain);
    }

    @Override
    public void save(KnowledgeTopic topic) {
        KnowledgeTopicDO doEntity = toDO(topic);
        if (mapper.selectById(doEntity.getId()) != null) {
            mapper.updateById(doEntity);
        } else {
            mapper.insert(doEntity);
        }
    }

    @Override
    public void deleteById(String id) {
        mapper.deleteById(id);
    }

    private KnowledgeTopic toDomain(KnowledgeTopicDO doEntity) {
        if (doEntity == null) return null;
        return KnowledgeTopic.builder()
                .id(doEntity.getId())
                .name(doEntity.getName())
                .icon(doEntity.getIcon())
                .description(doEntity.getDescription())
                .visibleScope(doEntity.getVisibleScope())
                .templateName(doEntity.getTemplateName())
                .createTime(doEntity.getCreateTime())
                .updateTime(doEntity.getUpdateTime())
                .build();
    }

    private KnowledgeTopicDO toDO(KnowledgeTopic topic) {
        if (topic == null) return null;
        KnowledgeTopicDO doEntity = new KnowledgeTopicDO();
        doEntity.setId(topic.getId());
        doEntity.setName(topic.getName());
        doEntity.setIcon(topic.getIcon());
        doEntity.setDescription(topic.getDescription());
        doEntity.setVisibleScope(topic.getVisibleScope());
        doEntity.setTemplateName(topic.getTemplateName());
        doEntity.setCreateTime(topic.getCreateTime());
        doEntity.setUpdateTime(topic.getUpdateTime());
        return doEntity;
    }
}
