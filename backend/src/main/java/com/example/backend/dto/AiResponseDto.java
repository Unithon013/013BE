package com.example.backend.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiResponseDto {
    private String name;
    private String age; // '60대 후반'과 같이 소개하는 경우를 위해 String 으로 설정해둠 (Integer로 나중에 변경해도 됨)
    private String hobbies; // AI 측에서 JSON 문자열로 준다고 가정
    private String gender; // AI 측에서 'M'또는 'F'로 준다고 가정
}