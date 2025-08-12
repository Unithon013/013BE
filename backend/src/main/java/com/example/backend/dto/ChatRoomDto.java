package com.example.backend.dto;

import com.example.backend.entity.Room;
import com.example.backend.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatRoomDto {

    private Long roomId;
    private String lastMessage;
    private Long opponentUserId;
    private String opponentName;

    // 상대방 위치, 나이, 취미/관심사리스트, 프로필 이미지 URL 등 추가
    private String opponentLocation;
    private String opponentAge;
    private List<String> opponentHobbies;
    private String opponentProfileUrl;

    public ChatRoomDto(Room room, User opponent) {
        this.roomId = room.getId();
        this.lastMessage = room.getLastMessage();
        this.opponentUserId = opponent.getId();
        this.opponentName = opponent.getName();
        this.opponentLocation = opponent.getLocation();
        this.opponentAge = opponent.getAge();
        this.opponentProfileUrl = opponent.getProfileUrl();

        // hobbies가 JSON 문자열이라면 파싱
        try {
            if (opponent.getHobbies() != null && !opponent.getHobbies().isBlank()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                this.opponentHobbies = mapper.readValue(opponent.getHobbies(), new com.fasterxml.jackson.core.type.TypeReference<>() {});
            }
        } catch (Exception e) {
            this.opponentHobbies = null; // 파싱 실패 시 null
        }
    }
}
