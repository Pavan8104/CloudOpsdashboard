package com.cloudops.dashboard.controller;

import com.cloudops.dashboard.dto.ChatRequest;
import com.cloudops.dashboard.dto.ChatResponse;
import com.cloudops.dashboard.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/message")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        log.debug("Chatbot message received, intent will be resolved by service");
        ChatResponse response = chatbotService.process(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER', 'VIEWER')")
    public ResponseEntity<ChatResponse> health() {
        ChatResponse response = ChatResponse.builder()
                .message("CloudOps Assistant is online and ready to help.")
                .intent("health")
                .build();
        return ResponseEntity.ok(response);
    }
}
