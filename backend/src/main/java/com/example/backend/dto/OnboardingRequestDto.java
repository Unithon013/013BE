package com.example.backend.dto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class OnboardingRequestDto {
    private MultipartFile video;
    private Double latitude;
    private Double longitude;
    private String profileUrl;
}
