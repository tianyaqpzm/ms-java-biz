package com.dark.aiagent.interfaces.chat.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dark.aiagent.domain.chat.entity.ChatMessage;
import com.dark.aiagent.application.chat.dto.ChatSessionDto;
import com.dark.aiagent.application.chat.service.ChatApplicationService;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Chat History Console")
@RestController
@RequestMapping("/rest/dark/v1/history")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatHistoryController {

    private final ChatApplicationService chatApplicationService;

    @Operation(summary = "Get chat history by session ID")
    @GetMapping
    public ResponseEntity<List<ChatMessage>> getHistory(@RequestParam String sessionId) {
        return ResponseEntity.ok(chatApplicationService.getChatHistory(sessionId));
    }

    @Operation(summary = "Get all chat sessions")
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionDto>> getSessions() {
        return ResponseEntity.ok(chatApplicationService.getAllSessions());
    }
}
