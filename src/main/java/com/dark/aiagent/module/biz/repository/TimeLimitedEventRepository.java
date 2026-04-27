package com.dark.aiagent.module.biz.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.dark.aiagent.module.biz.entity.TimeLimitedEvent;

@Repository
public interface TimeLimitedEventRepository extends MongoRepository<TimeLimitedEvent, String> {
}
