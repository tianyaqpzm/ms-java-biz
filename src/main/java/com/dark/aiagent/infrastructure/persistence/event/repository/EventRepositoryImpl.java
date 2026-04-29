package com.dark.aiagent.infrastructure.persistence.event.repository;

import com.dark.aiagent.domain.event.entity.TimeLimitedEvent;
import com.dark.aiagent.domain.event.repository.EventRepository;
import com.dark.aiagent.domain.event.valueobject.EventAppearance;
import com.dark.aiagent.infrastructure.persistence.event.entity.TimeLimitedEventDO;
import com.dark.aiagent.infrastructure.persistence.event.mapper.MongoEventMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class EventRepositoryImpl implements EventRepository {

    private final MongoEventMapper mongoEventMapper;

    public EventRepositoryImpl(MongoEventMapper mongoEventMapper) {
        this.mongoEventMapper = mongoEventMapper;
    }

    @Override
    public TimeLimitedEvent save(TimeLimitedEvent event) {
        TimeLimitedEventDO savedDO = mongoEventMapper.save(toDO(event));
        return toDomain(savedDO);
    }

    @Override
    public List<TimeLimitedEvent> findAll() {
        return mongoEventMapper.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TimeLimitedEvent> findById(String id) {
        return mongoEventMapper.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(String id) {
        mongoEventMapper.deleteById(id);
    }

    private TimeLimitedEvent toDomain(TimeLimitedEventDO doEntity) {
        if (doEntity == null) return null;
        EventAppearance appearance = null;
        if (doEntity.getAppearance() != null) {
            appearance = new EventAppearance(doEntity.getAppearance().getType(), doEntity.getAppearance().getValue());
        }
        return TimeLimitedEvent.restore(
                doEntity.getId(),
                doEntity.getTitle(),
                doEntity.getCategory(),
                doEntity.getDate(),
                doEntity.getTime(),
                doEntity.getDescription(),
                doEntity.getRepeatYearly(),
                appearance,
                doEntity.getCreatedAt()
        );
    }

    private TimeLimitedEventDO toDO(TimeLimitedEvent domain) {
        if (domain == null) return null;
        TimeLimitedEventDO.AppearanceDO appearanceDO = null;
        if (domain.getAppearance() != null) {
            appearanceDO = TimeLimitedEventDO.AppearanceDO.builder()
                    .type(domain.getAppearance().type())
                    .value(domain.getAppearance().value())
                    .build();
        }
        return TimeLimitedEventDO.builder()
                .id(domain.getId())
                .title(domain.getTitle())
                .category(domain.getCategory())
                .date(domain.getDate())
                .time(domain.getTime())
                .description(domain.getDescription())
                .repeatYearly(domain.getRepeatYearly())
                .appearance(appearanceDO)
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
