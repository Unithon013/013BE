package com.example.backend.controller;

import com.example.backend.dto.AdditionalRecommendationRequestDto;
import com.example.backend.dto.RecommendedUserDto;
import com.example.backend.entity.Recommendation;
import com.example.backend.entity.Room;
import com.example.backend.service.ChatService;
import com.example.backend.service.RecommendationService;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;
    private final ChatService chatService;
    private final UserService userService;

//    @GetMapping
//    public List<Recommendation> getRecommendations() {
//        return recommendationService.findAll();
//    }

    // 해커톤에서는 편의상 Header로 현재 사용자 ID 받기 ( || JWT)
    // 무료 추천 받기 API
    @GetMapping
    public ResponseEntity<List<RecommendedUserDto>> getRecommendations(@RequestHeader("X-User-Id") Long currentUserId) {
        List<RecommendedUserDto> recommendations = recommendationService.getRecommendationsForUser(currentUserId);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping
    public Recommendation createRecommendation(@RequestBody Recommendation rec) {
        return recommendationService.save(rec);
    }


    @PostMapping("/{targetUserId}/contact")
    public ResponseEntity<Room> contactUser(
            @RequestHeader("X-User-Id") Long currentUserId,
            @PathVariable Long targetUserId) {

        userService.deductPoints(currentUserId, 10);

        Room createdRoom = chatService.createChatRoomAndSendVideo(currentUserId, targetUserId);
        return ResponseEntity.ok(createdRoom);
    }

    /**
     * 포인트를 사용해 추가 추천을 받는 API
     * @param currentUserId 현재 사용자 ID
     * @param requestDto 추가로 추천받을 인원 수가 담긴 요청 DTO
     * @return 새로 추천된 사용자 목록
     */
    @PostMapping("/additional")
    public ResponseEntity<List<RecommendedUserDto>> getAdditionalRecommendations(
            @RequestHeader("X-User-Id") Long currentUserId,
            @RequestBody AdditionalRecommendationRequestDto requestDto) {

        // 포인트 차감 로직 (ex. 추가 추천 시 20 포인트)
        // 1명당 20 포인트라고 가정하고, 요청한 인원수만큼 포인트 차감
        int pointsToDeduct = requestDto.getCount() * 20;
        userService.deductPoints(currentUserId, pointsToDeduct);

        // 서비스 호출
        List<RecommendedUserDto> newRecommendations = recommendationService.getAdditionalRecommendations(currentUserId, requestDto.getCount());
        return ResponseEntity.ok(newRecommendations);
    }
}
