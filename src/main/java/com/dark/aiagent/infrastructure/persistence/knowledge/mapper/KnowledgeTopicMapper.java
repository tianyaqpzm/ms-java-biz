package com.dark.aiagent.infrastructure.persistence.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dark.aiagent.infrastructure.persistence.knowledge.entity.KnowledgeTopicDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgeTopicMapper extends BaseMapper<KnowledgeTopicDO> {
}
