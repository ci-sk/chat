package org.example.entity.dto;

import lombok.Data;

@Data
public class ChatMessage {
    private String type;
    private String targetUserId;
    private String content;
}
