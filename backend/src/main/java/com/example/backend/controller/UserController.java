package com.example.backend.controller;

import com.example.backend.dto.OnboardingRequestDto;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 로깅용 Logger 객체
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    // @RequestBody 대신 @ModelAttribute를 사용하면 파일과 JSON 데이터를 함께 받을 수 있음
    @PostMapping("/onboarding")
    public ResponseEntity<User> onboarding(@ModelAttribute OnboardingRequestDto requestDto) {
        log.info(">>>>> [API CALL] /users/onboarding 시작");
        if (requestDto.getVideo() == null || requestDto.getVideo().isEmpty()) {
            log.error(">>>>> [ERROR] 비디오 파일이 비어있습니다.");
            return ResponseEntity.badRequest().build();
        }
        log.info(">>>>> 비디오 파일 수신 완료: {}", requestDto.getVideo().getOriginalFilename());
        User newUser = userService.onboardUser(requestDto);
        log.info(">>>>> 사용자 온보딩 성공, ID: {}", newUser.getId());
        return ResponseEntity.ok(newUser);
    }

    /**
     * @RequestParam 대신 @RequestHeader를 사용하여 헤더에서 사용자 ID를 받도록 수정
     */
    @GetMapping("/me")
    public ResponseEntity<User> getMyInfo(@RequestHeader("X-User-Id") Long userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(user);
    }

}