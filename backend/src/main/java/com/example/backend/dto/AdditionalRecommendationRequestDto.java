package com.example.backend.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdditionalRecommendationRequestDto {
    // 추가로 추천받고 싶은 인원 수
    private int count;
}
