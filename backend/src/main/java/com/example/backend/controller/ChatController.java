package com.example.backend.controller;
import com.example.backend.dto.ChatRoomDto;
import com.example.backend.dto.MessageRequestDto;
import com.example.backend.entity.Message;
import com.example.backend.entity.Room;
import com.example.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/{chatId}/messages")
    public List<Message> getMessages(@PathVariable Long chatId) {
        return chatService.getMessages(chatId);
    }

    @PostMapping("/{chatId}/message")
    public ResponseEntity<Message> sendMessage(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long chatId,
            @RequestBody MessageRequestDto requestDto) {

        Message savedMessage = chatService.sendMessage(chatId, currentUserId, requestDto);
        return ResponseEntity.ok(savedMessage);
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