package com.example.backend.dto;

import com.example.backend.entity.Room;
import com.example.backend.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class ChatRoomDto {

    private Long roomId;
    private String lastMessage;
    private Long opponentUserId;
    private String opponentName;

    // 필요하다면 상대방 프로필 이미지 URL 등 추가

    public ChatRoomDto(Room room, User opponent) {
        this.roomId = room.getId();
        this.lastMessage = room.getLastMessage();
        this.opponentUserId = opponent.getId();
        this.opponentName = opponent.getName();
    }
}