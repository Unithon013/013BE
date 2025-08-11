package com.example.backend.service;

import com.example.backend.dto.RecommendedUserDto;
import com.example.backend.entity.Recommendation;
import com.example.backend.entity.User;
import com.example.backend.exception.UserNotFoundException;
import com.example.backend.repository.RecommendationRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private static final int DAILY_RECOMMENDATION_LIMIT = 3;

    @Transactional // 이 어노테이션으로 전체 메소드가 하나의 트랜잭션으로 묶임
    public List<RecommendedUserDto> purchaseAdditionalRecommendations(Long currentUserId, int count) {

        // 1. 포인트 차감 로직 (ex. 추가 추천 시 20 포인트)
        // 1명당 20 포인트라고 가정하고, 요청한 인원수만큼 포인트 차감
        int pointsToDeduct = count * 20;
        userService.deductPoints(currentUserId, pointsToDeduct);

        // 2. 추가 추천 로직 실행
        // 포인트 차감 + 추천 -> 예외 시 묶어서 자동 롤백 (트랜잭션 원자적으로)
        return getAdditionalRecommendations(currentUserId, count);
    }

    /**
     * 포인트를 사용해 요청한 인원수만큼 사용자를 추가로 추천하는 메소드
     * @param currentUserId 현재 사용자 ID
     * @param count 추가로 추천받을 인원 수
     * @return 새로 추천된 사용자 정보 목록
     */
    @Transactional
    public List<RecommendedUserDto> getAdditionalRecommendations(Long currentUserId, int count) {
        // 1. 현재 사용자 정보 및 오늘 추천받은 사용자 목록 조회
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        List<Long> alreadyRecommendedIds = recommendationRepository.findRecommendedUserIdsByUserIdAndDate(currentUserId, LocalDate.now());

        // 2. 제외할 ID 목록 생성 (본인 + 이미 추천한 모든 사람)
        List<Long> excludeUserIds = Stream.concat(
                Stream.of(currentUserId),
                alreadyRecommendedIds.stream()
        ).collect(Collectors.toList());

        // 3. 위치 기반으로 '요청한 인원(count)' 만큼 새로운 추천 대상 검색
        List<User> newRecommendedUsers = userRepository.findUsersNearBy(
                currentUser.getLatitude(),
                currentUser.getLongitude(),
                50.0, // 50km 반경
                excludeUserIds,
                count // 요청받은 인원수만큼 LIMIT 설정
        );
        
        // 필요 시 구현하기
//        if (newRecommendedUsers.size() < count){
//            // 포인트 회복 처리 로직
//            // 해커톤 레벨에서는 사용할 일 없을 듯
//        }

        if (newRecommendedUsers.isEmpty()) {
//            throw new RuntimeException("No more users to recommend."); // 추천할 사람이 더 없을 때 예외 처리
            // 반환 대상 부족 시 예외 발생 대신 빈 배열 반환하기 - 프론트에서 처리
            return Collections.emptyList();
        }

        // 4. 새로운 추천 기록을 DB에 저장
        newRecommendedUsers.forEach(recommendedUser -> {
            Recommendation newLog = Recommendation.builder()
                    .user(currentUser)
                    .recommendedUser(recommendedUser)
                    .date(LocalDate.now())
                    .build();
            recommendationRepository.save(newLog);
        });

        // 5. 새로 추천된 사용자 목록을 DTO로 변환하여 반환
        return newRecommendedUsers.stream()
                .map(RecommendedUserDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<RecommendedUserDto> getRecommendationsForUser(Long currentUserId) {
        // 1. 현재 사용자 정보 조회
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        LocalDate today = LocalDate.now();

        // 2. 오늘 이미 추천받은 기록이 있는지 확인
        List<Long> alreadyRecommendedIds = recommendationRepository.findRecommendedUserIdsByUserIdAndDate(currentUserId, today);

        // 3. 이미 3명 이상 추천받았다면, 기존 추천 목록을 그대로 반환
        if (alreadyRecommendedIds.size() >= DAILY_RECOMMENDATION_LIMIT) {
            List<User> recommendedUsers = userRepository.findAllById(alreadyRecommendedIds);
            return recommendedUsers.stream()
                    .map(RecommendedUserDto::new)
                    .collect(Collectors.toList());
        }

        // 4. 새로 추천해야 할 인원 수 계산
        int neededRecommendations = DAILY_RECOMMENDATION_LIMIT - alreadyRecommendedIds.size();

        // 5. 제외할 ID 목록 생성 (본인 + 이미 추천한 사람)
        List<Long> excludeUserIds = Stream.concat(
                Stream.of(currentUserId),
                alreadyRecommendedIds.stream()
        ).collect(Collectors.toList());


        // 6. 위치 기반으로 새로운 추천 대상 '후보군'을 넉넉하게 검색 (예: 20명)
        List<User> candidates = userRepository.findUsersNearBy(
                currentUser.getLatitude(),
                currentUser.getLongitude(),
                50.0,
                excludeUserIds,
                20 // 필요한 3명보다 넉넉하게 후보군 확보
        );

        // 7. 후보군 중에서 '나'와 취미가 가장 비슷한 순서로 정렬
        candidates.sort((u1, u2) -> {
            long u1Matches = countMatchingHobbies(currentUser, u1);
            long u2Matches = countMatchingHobbies(currentUser, u2);
            return Long.compare(u2Matches, u1Matches); // 내림차순 정렬
        });

        // 8. 정렬된 후보군 중에서 필요한 만큼만 최종 선택
        List<User> newRecommendedUsers = candidates.stream()
                .limit(neededRecommendations)
                .toList();

        // 9. 새로운 추천 기록을 DB에 저장 (기존 7번 로직)
        newRecommendedUsers.forEach(recommendedUser -> {
            Recommendation newLog = Recommendation.builder()
                    .user(currentUser)
                    .recommendedUser(recommendedUser)
                    .date(today)
                    .build();
            recommendationRepository.save(newLog);
        });

        // 10. 최종 추천 목록 DTO로 변환하여 반환 (기존 8번 로직)
        List<User> finalRecommendedUsers = userRepository.findAllById(
                Stream.concat(alreadyRecommendedIds.stream(), newRecommendedUsers.stream().map(User::getId))
                        .collect(Collectors.toList())
        );

        return finalRecommendedUsers.stream()
                .map(RecommendedUserDto::new)
                .collect(Collectors.toList());
    }

    public List<Recommendation> findAll() {
        return recommendationRepository.findAll();
    }

    public Recommendation save(Recommendation rec) {
        return recommendationRepository.save(rec);
    }

    // hobbies 필드(JSON 문자열)를 파싱하여 겹치는 취미 개수를 세는 헬퍼 메소드
    private long countMatchingHobbies(User user1, User user2) {
//        String hobbies1 = user1.getHobbies().replaceAll("[\"\\[\\]\\s]", "");
//        String hobbies2 = user2.getHobbies().replaceAll("[\"\\[\\]\\s]", "");
//
//        List<String> list1 = List.of(hobbies1.split(","));
//        List<String> list2 = List.of(hobbies2.split(","));
//
//        return list1.stream().filter(list2::contains).count();

        ObjectMapper mapper = new ObjectMapper();
        try {
            // hobbies가 null이거나 비어있을 경우를 대비
            if (user1.getHobbies() == null || user2.getHobbies() == null ||
                    user1.getHobbies().isBlank() || user2.getHobbies().isBlank()) {
                return 0;
            }
            List<String> hobbies1 = mapper.readValue(user1.getHobbies(), new TypeReference<>() {});
            List<String> hobbies2 = mapper.readValue(user2.getHobbies(), new TypeReference<>() {});

            // 공통된 요소의 개수를 효율적으로 계산
            hobbies1.retainAll(hobbies2);
            return hobbies1.size();
        } catch (Exception e) {
            // JSON 파싱 실패 시 로그를 남기고 0을 반환하여 서비스 중단을 방지
            // log.error("Failed to parse hobbies", e);
            return 0;
        }
    }
}