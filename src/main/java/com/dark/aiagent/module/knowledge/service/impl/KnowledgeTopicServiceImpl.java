package com.dark.aiagent.module.knowledge.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dark.aiagent.module.knowledge.entity.KnowledgeTopic;
import com.dark.aiagent.module.knowledge.mapper.KnowledgeTopicMapper;
import com.dark.aiagent.module.knowledge.service.KnowledgeTopicService;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeTopicServiceImpl extends ServiceImpl<KnowledgeTopicMapper, KnowledgeTopic> implements KnowledgeTopicService {
}
