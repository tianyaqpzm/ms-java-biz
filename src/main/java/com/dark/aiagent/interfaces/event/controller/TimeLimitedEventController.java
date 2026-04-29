package com.dark.aiagent.interfaces.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dark.aiagent.application.event.dto.EventDto;
import com.dark.aiagent.application.event.service.EventApplicationService;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Time Limited Event Console")
@RestController
@RequestMapping("/rest/dark/v1/time-limit-events")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TimeLimitedEventController {

    private final EventApplicationService eventApplicationService;

    @Operation(summary = "Create an event")
    @PostMapping
    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto event) {
        return ResponseEntity.ok(eventApplicationService.createEvent(event));
    }

    @Operation(summary = "Get all events")
    @GetMapping
    public ResponseEntity<List<EventDto>> getAllEvents() {
        return ResponseEntity.ok(eventApplicationService.getAllEvents());
    }

    @Operation(summary = "Get event by id")
    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEventById(@PathVariable String id) {
        return eventApplicationService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update an event")
    @PutMapping("/{id}")
    public ResponseEntity<EventDto> updateEvent(@PathVariable String id, @RequestBody EventDto event) {
        return ResponseEntity.ok(eventApplicationService.updateEvent(id, event));
    }

    @Operation(summary = "Delete an event")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String id) {
        eventApplicationService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
