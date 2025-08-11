package com.example.backend.dto;

import com.example.backend.entity.Message;
import com.example.backend.entity.Participant;
import com.example.backend.entity.Room;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class RoomResponseDto {
    private Long id;
    private String lastMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ParticipantDto> participants; // Participant 정보
    private List<MessageDto> messages;         // Message 정보

    // Room 엔티티를 DTO로 변환하는 생성자
    public RoomResponseDto(Room room) {
        this.id = room.getId();
        this.lastMessage = room.getLastMessage();
        this.createdAt = room.getCreatedAt();
        this.updatedAt = room.getUpdatedAt();

        // 순환 참조를 피하기 위해 Participant와 Message도 DTO로 변환
        this.participants = room.getParticipants() == null ? Collections.emptyList() :
                room.getParticipants().stream().map(ParticipantDto::new).collect(Collectors.toList());

        this.messages = room.getMessages() == null ? Collections.emptyList() :
                room.getMessages().stream().map(MessageDto::new).collect(Collectors.toList());

    }

    // Participant 정보를 담을 내부 DTO
    @Getter
    static class ParticipantDto {
        private Long id;
        private Long userId;
        // 필요하다면 User의 다른 정보도 추가 가능
        // private String userName;

        public ParticipantDto(Participant participant) {
            this.id = participant.getId();
            this.userId = participant.getUser().getId();
            // this.userName = participant.getUser().getName();
        }
    }

    // Message 정보를 담을 내부 DTO
    @Getter
    static class MessageDto {
        private Long id;
        private Long senderId;
        private String messageContent;
        private Message.MessageType messageType;
        private LocalDateTime createdAt;

        public MessageDto(Message message) {
            this.id = message.getId();
            this.senderId = message.getSenderId();
            this.messageContent = message.getMessageContent();
            this.messageType = message.getMessageType();
            this.createdAt = message.getCreatedAt();
        }
    }
}