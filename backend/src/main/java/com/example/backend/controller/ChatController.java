package com.example.backend.controller;
import com.example.backend.dto.ChatRoomDto;
import com.example.backend.dto.MessageRequestDto;
import com.example.backend.dto.MessageResponseDto;
import com.example.backend.entity.Message;
import com.example.backend.entity.Room;
import com.example.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<MessageResponseDto>> getMessages(@PathVariable Long chatId) {
        return ResponseEntity.ok(chatService.getMessages(chatId).stream()
                .map(MessageResponseDto::new)
                .collect(Collectors.toList()));
    }

    @PostMapping("/{chatId}/message")
    public ResponseEntity<List<MessageResponseDto>> sendMessage(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long chatId,
            @RequestBody MessageRequestDto requestDto) {

        chatService.sendMessage(chatId, currentUserId, requestDto);

        // 컨트롤러에서 엔티티를 DTO로 변환하여 클라이언트에게 응답
        // ** 수정: 우선은 Service 레벨에서 해당 채팅방의 전체 메세지 리스트 반환
        return ResponseEntity.ok(chatService.getMessages(chatId).stream()
                .map(MessageResponseDto::new)
                .collect(Collectors.toList()));
    }

    @PostMapping("/rooms")
    public Room createRoom(@RequestBody Room room) {
        return chatService.saveRoom(room);
    }

    // 내 채팅방 목록 조회 API
    @GetMapping
    public ResponseEntity<List<ChatRoomDto>> getMyChatRooms(@RequestHeader("X-User-Id") Long currentUserId) {
        List<ChatRoomDto> myRooms = chatService.findMyChatRooms(currentUserId);
        return ResponseEntity.ok(myRooms);
    }
}