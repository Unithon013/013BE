package com.example.backend.service;

import com.example.backend.dto.RecommendedUserDto;
import com.example.backend.entity.Recommendation;
import com.example.backend.entity.User;
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

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private static final int DAILY_RECOMMENDATION_LIMIT = 3;

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
                .orElseThrow(() -> new RuntimeException("Current user not found"));

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
        // 간단한 구현을 위해, 실제로는 JSON 파싱 라이브러리(Gson, Jackson) 사용해도 됨
        String hobbies1 = user1.getHobbies().replaceAll("[\"\\[\\]\\s]", "");
        String hobbies2 = user2.getHobbies().replaceAll("[\"\\[\\]\\s]", "");

        List<String> list1 = List.of(hobbies1.split(","));
        List<String> list2 = List.of(hobbies2.split(","));

        return list1.stream().filter(list2::contains).count();
    }
}