package com.example.backend.controller;

import com.example.backend.dto.OnboardingRequestDto;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // @RequestBody 대신 @ModelAttribute를 사용하면 파일과 JSON 데이터를 함께 받을 수 있음
    @PostMapping("/onboarding")
    public ResponseEntity<User> onboarding(@ModelAttribute OnboardingRequestDto requestDto) {
        User newUser = userService.onboardUser(requestDto);
        return ResponseEntity.ok(newUser);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMyInfo(@RequestParam Long userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(user);
    }

}