package com.dark.aiagent.domain.event.repository;

import com.dark.aiagent.domain.event.entity.TimeLimitedEvent;
import java.util.List;
import java.util.Optional;

public interface EventRepository {

    TimeLimitedEvent save(TimeLimitedEvent event);

    List<TimeLimitedEvent> findAll();

    Optional<TimeLimitedEvent> findById(String id);

    void deleteById(String id);
}
