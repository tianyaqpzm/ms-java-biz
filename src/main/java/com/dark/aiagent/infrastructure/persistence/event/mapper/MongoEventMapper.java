package com.dark.aiagent.infrastructure.persistence.event.mapper;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.dark.aiagent.infrastructure.persistence.event.entity.TimeLimitedEventDO;

/**
 * MongoDB Repository 映射
 */
@Repository
public interface MongoEventMapper extends MongoRepository<TimeLimitedEventDO, String> {
}
