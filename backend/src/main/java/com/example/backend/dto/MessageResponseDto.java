package com.example.backend.dto;

import com.example.backend.entity.Message;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageResponseDto {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String messageContent;
    private String messageType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Message 엔티티를 DTO로 변환하는 생성자
    public MessageResponseDto(Message message) {
        this.id = message.getId();
        this.roomId = message.getRoom().getId(); // 프록시 객체라도 ID는 가지고 있으므로 안전하게 호출 가능
        this.senderId = message.getSenderId();
        this.messageContent = message.getMessageContent();
        this.messageType = String.valueOf(message.getMessageType());
        this.createdAt = message.getCreatedAt();
        this.updatedAt = message.getUpdatedAt();
    }
}