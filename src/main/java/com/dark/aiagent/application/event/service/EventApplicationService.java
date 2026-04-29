package com.dark.aiagent.application.event.service;

import com.dark.aiagent.application.event.dto.EventDto;
import com.dark.aiagent.domain.event.entity.TimeLimitedEvent;
import com.dark.aiagent.domain.event.repository.EventRepository;
import com.dark.aiagent.domain.event.valueobject.EventAppearance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventApplicationService {

    private final EventRepository eventRepository;

    public EventApplicationService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public EventDto createEvent(EventDto dto) {
        EventAppearance appearance = null;
        if (dto.getAppearance() != null) {
            appearance = new EventAppearance(dto.getAppearance().getType(), dto.getAppearance().getValue());
        }
        TimeLimitedEvent event = TimeLimitedEvent.create(
                dto.getTitle(),
                dto.getCategory(),
                dto.getDate(),
                dto.getTime(),
                dto.getDescription(),
                dto.getRepeatYearly(),
                appearance
        );
        return toDto(eventRepository.save(event));
    }

    public List<EventDto> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Optional<EventDto> getEventById(String id) {
        return eventRepository.findById(id).map(this::toDto);
    }

    public EventDto updateEvent(String id, EventDto dto) {
        TimeLimitedEvent existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + id));
        
        EventAppearance appearance = null;
        if (dto.getAppearance() != null) {
            appearance = new EventAppearance(dto.getAppearance().getType(), dto.getAppearance().getValue());
        }
        
        existingEvent.update(
                dto.getTitle(),
                dto.getCategory(),
                dto.getDate(),
                dto.getTime(),
                dto.getDescription(),
                dto.getRepeatYearly(),
                appearance
        );
        return toDto(eventRepository.save(existingEvent));
    }

    public void deleteEvent(String id) {
        eventRepository.deleteById(id);
    }

    private EventDto toDto(TimeLimitedEvent domain) {
        EventDto dto = new EventDto();
        dto.setId(domain.getId());
        dto.setTitle(domain.getTitle());
        dto.setCategory(domain.getCategory());
        dto.setDate(domain.getDate());
        dto.setTime(domain.getTime());
        dto.setDescription(domain.getDescription());
        dto.setRepeatYearly(domain.getRepeatYearly());
        dto.setCreatedAt(domain.getCreatedAt());
        if (domain.getAppearance() != null) {
            EventDto.AppearanceDto appDto = new EventDto.AppearanceDto();
            appDto.setType(domain.getAppearance().type());
            appDto.setValue(domain.getAppearance().value());
            dto.setAppearance(appDto);
        }
        return dto;
    }
}
