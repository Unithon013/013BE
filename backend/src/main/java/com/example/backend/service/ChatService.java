package com.example.backend.service;

import com.example.backend.dto.ChatRoomDto;
import com.example.backend.dto.MessageRequestDto;
import com.example.backend.entity.Message;
import com.example.backend.entity.Participant;
import com.example.backend.entity.Room;
import com.example.backend.entity.User;
import com.example.backend.repository.MessageRepository;
import com.example.backend.repository.ParticipantRepository;
import com.example.backend.repository.RoomRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final RoomRepository roomRepository;
    private final MessageRepository messageRepository;
    private final ParticipantRepository participantRepository;
    private final UserRepository userRepository;

    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    public List<Message> getMessages(Long roomId) {
        return messageRepository.findByRoomId(roomId);
    }

    @Transactional
    public Message sendMessage(Long roomId, Long senderId, MessageRequestDto requestDto) {
        // 1. 채팅방 정보 조회
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // 2. 메시지 객체 생성
        Message newMessage = Message.builder()
                .room(room)
                .senderId(senderId)
                .messageContent(requestDto.getMessageContent())
                .messageType(requestDto.getMessageType())
                .build();

        // 3. 메시지 저장
        messageRepository.save(newMessage);

        // 4. 채팅방의 마지막 메시지 업데이트
        String lastMessage = requestDto.getMessageType() == Message.MessageType.TEXT ?
                requestDto.getMessageContent() : "영상이 도착했습니다.";
        room.setLastMessage(lastMessage);
        roomRepository.save(room);

        return newMessage;
    }

    /**
     * 채팅방 목록을 조회하는 로직
     * @param currentUserId 사용자
     * @return 참여중인 채팅방 목록
     */
    @Transactional(readOnly = true)
    public List<ChatRoomDto> findMyChatRooms(Long currentUserId) {
        // 1. 내가 참여하고 있는 모든 Participant 정보를 가져온다.
        List<Participant> myParticipants = participantRepository.findByUserId(currentUserId);

        List<ChatRoomDto> chatRoomDtos = new ArrayList<>();

        // 2. 각 참여 정보에서 채팅방(Room)과 상대방(Opponent) 정보를 찾는다.
        for (Participant myParticipant : myParticipants) {
            Room room = myParticipant.getRoom();
            // 채팅방에서 내가 아닌 다른 참가자를 찾는다.
            Participant opponentParticipant = participantRepository.findByRoomIdAndUserIdNot(room.getId(), currentUserId)
                    .orElse(null); // 상대방이 없는 경우는 없겠지만 예외 처리

            if (opponentParticipant != null) {
                User opponent = opponentParticipant.getUser();
                chatRoomDtos.add(new ChatRoomDto(room, opponent));
            }
        }
        return chatRoomDtos;
    }

    /**
     * 두 사용자 간의 채팅방을 생성하고, 첫 메시지(자기소개 영상)를 보내는 로직
     * @param currentUserId 사용자
     * @param targetUserId 상대방
     * @return 새로 생성된 채팅방
     */
    @Transactional
    public Room createChatRoomAndSendVideo(Long currentUserId, Long targetUserId) {
        // 0. 사용자 정보 조회
        User currentUser = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("Current User not found"));
        User targetUser = userRepository.findById(targetUserId).orElseThrow(() -> new RuntimeException("Target User not found"));

        // 1. 새로운 채팅방 생성 및 저장
        Room newRoom = Room.builder().build();
        roomRepository.save(newRoom);

        // 2. 참가자 정보 생성 및 저장 (2명)
        Participant currentUserParticipant = Participant.builder().user(currentUser).room(newRoom).build();
        Participant targetUserParticipant = Participant.builder().user(targetUser).room(newRoom).build();
        participantRepository.save(currentUserParticipant);
        participantRepository.save(targetUserParticipant);

        // 3. 연락하는 사람의 자기소개 영상을 첫 메시지로 전송
        Message firstMessage = Message.builder()
                .room(newRoom)
                .senderId(currentUserId)
                .messageContent(currentUser.getVideoUrl()) // 영상 파일 경로를 메시지 내용으로
                .messageType(Message.MessageType.VIDEO)
                .build();
        messageRepository.save(firstMessage);

        // 4. 채팅방의 마지막 메시지 업데이트
        newRoom.setLastMessage("영상이 도착했습니다.");
        roomRepository.save(newRoom);

        return newRoom;
    }
}