package com.example.backend.service;

import com.example.backend.controller.UserController;
import com.example.backend.dto.AiResponseDto;
import com.example.backend.dto.OnboardingRequestDto;
import com.example.backend.entity.User;
import com.example.backend.exception.InsufficientPointsException;
import com.example.backend.exception.UserNotFoundException;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

@Service
@RequiredArgsConstructor
public class UserService {

    // 로깅용 Logger 객체
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private static final String DEFAULT_INTRO = "새로운 인연을 원하는 시니어입니다.";

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AiRequestService aiRequestService;
    private final LocationService locationService;

    @Transactional
    public User onboardUser(OnboardingRequestDto requestDto) {
        // 1. 영상 파일과 프로필 사진을 서버에 저장
        String videoPath = fileStorageService.storeFile(requestDto.getVideo());

        // 2. 좌표 -> 지역명 변환 (실제 사용 시 이 부분 활성화; 테스트: 더미로 처리)
        // String cityAndDistrict = locationService.getCityAndDistrict(requestDto.getLatitude(), requestDto.getLongitude());

        // 3. AI 분석 전, 'PROCESSING' 상태로 기본 User 정보만 먼저 생성하고 DB에 저장
        User newUser = User.builder()
                .videoUrl(videoPath)
//                .profileUrl(requestDto.getProfileUrl())
                //.latitude(requestDto.getLatitude())
                //.longitude(requestDto.getLongitude())
                //.location(cityAndDistrict)
                .status(User.Status.PROCESSING) // 처리중 상태로
                .point(100)
                .build();

        User savedUser = userRepository.save(newUser);

        // 4. 실제 AI 분석은 비동기 메서드에 위임하고, 컨트롤러에는 즉시 사용자 객체를 반환
        // processAiAnalysisInBackground(savedUser, videoPath);
        // ** 수정: 비동기 메서드에 User 객체 대신 'ID'를 전달
        processAiAnalysisInBackground(savedUser.getId(), videoPath);

        return savedUser;
    }

    @Async // 이 메서드는 별도의 스레드에서 실행됩니다.
    @Transactional
    public void processAiAnalysisInBackground(Long userId, String videoPath) {
        // ** 수정: 전달받은 ID로 DB에서 '실제' User 엔티티를 다시 조회합니다.
        // 이렇게 하면 이 'user' 객체는 현재 트랜잭션에서 관리되는 "라이브" 상태가 됩니다.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("AI 분석 중 사용자를 찾을 수 없습니다: " + userId));

        try {
            // File videoFile = fileStorageService.getFileByPath(videoPath);
            File videoFile = fileStorageService.getFile(fileStorageService.getFileStorageLocation().resolve(videoPath.substring("/media/" .length())).toString());

            // 1. AI 서버에 분석 요청 후 task_id 받기
            String taskId = aiRequestService.requestVideoProcessing(videoFile);

            // 2. 작업이 완료될 때까지 5초 간격으로 폴링
            for (int i = 0; i < 60; i++) { // 최대 5분(60 * 5초) 대기
                Thread.sleep(5000); // 5초 대기
                AiResponseDto aiResponse = aiRequestService.getProcessingResult(taskId);

                if (aiResponse != null) {
                    // 3. 결과 받으면 User 정보 업데이트 후 루프 종료
                    user.setName(aiResponse.getName() == null ? "미상" : aiResponse.getName());
                    user.setAge(aiResponse.getAge() == null ? "미상" : aiResponse.getAge());
                    user.setHobbies(aiResponse.getHobbies());


                    //user.setGender(aiResponse.getGender() == null ? User.Gender.F : User.Gender.valueOf(aiResponse.getGender())); // default=F로 설정
                    if (aiResponse.getGender() != null && !aiResponse.getGender().isBlank()) {
                        try {
                            user.setGender(User.Gender.valueOf(aiResponse.getGender().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            log.warn("AI 서버로부터 유효하지 않은 gender 값 수신: {}", aiResponse.getGender());
                        }
                    }

                    user.setIntroduction(aiResponse.getIntroduction() == null ? DEFAULT_INTRO : aiResponse.getIntroduction());
                    user.setStatus(User.Status.COMPLETE); // 상태를 '완료'로 변경
                    userRepository.save(user);

                    log.info(">>>>> AI 분석 및 사용자 정보 업데이트 성공, User ID: {}", user.getId());
                    return; // 성공했으므로 메서드 종료
                }
            }
            // [핵심 수정] for 루프가 모두 끝난 후에 타임아웃 예외를 발생시킵니다.
            throw new RuntimeException("AI 분석 작업 시간 초과, Task ID: " + taskId);

        } catch (Exception e) {
            log.error("[Task ID: " + user.getId() + "] AI 분석 비동기 처리 중 오류 발생", e);
            // 오류 발생 시 상태를 '실패'로 변경
            user.setStatus(User.Status.FAILED);
            userRepository.save(user);
        }
    }

    @Transactional
    public void deductPoints(Long userId, int pointsToDeduct) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int currentPoints = user.getPoint();
        if (currentPoints < pointsToDeduct) {
            throw new InsufficientPointsException("Not enough points"); // 실제로는 비즈니스 예외 처리 필요
        }

        user.setPoint(currentPoints - pointsToDeduct);
        userRepository.save(user);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id)); // 구체적인 예외 발생
    }
}