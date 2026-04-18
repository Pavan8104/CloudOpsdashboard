package com.cloudops.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ChatResponse {

    private String message;
    private String sessionId;
    private List<String> suggestions;
    private Instant timestamp;
    private String intent;
}
