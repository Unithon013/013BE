package com.example.backend.service;

import com.example.backend.dto.AiResponseDto;
import com.example.backend.dto.OnboardingRequestDto;
import com.example.backend.entity.User;
import com.example.backend.exception.InsufficientPointsException;
import com.example.backend.exception.UserNotFoundException;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AiRequestService aiRequestService;

    @Transactional
    public User onboardUser(OnboardingRequestDto requestDto) {
        // 1. 영상 파일 서버에 저장
        String videoPath = fileStorageService.storeFile(requestDto.getVideo());
        // 파일 형식 -> /media/파일명 형태로
//        File videoFile = fileStorageService.getFile(videoPath);
        File videoFile = fileStorageService.getFile(
                fileStorageService.getFileStorageLocation().resolve(videoPath.substring("/media/".length())).toString());

        // 2. 저장된 파일을 AI 서버로 보내 정보 추출
//        AiResponseDto aiResponse = aiRequestService.processVideo(videoFile); // ai 사용 시 이 부분으로 활성화 변경하기
        AiResponseDto aiResponse = aiRequestService.processVideoDev(videoFile);

        // 3. 받은 정보로 User 객체 생성 및 DB 저장
        User newUser = User.builder()
                .name(aiResponse.getName())
                .age(aiResponse.getAge())
                .hobbies(aiResponse.getHobbies())
                .videoUrl(videoPath) // 서버에 저장된 로컬 경로
                .latitude(requestDto.getLatitude())
                .longitude(requestDto.getLongitude())
                .point(100)
                .build();

        return userRepository.save(newUser);
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