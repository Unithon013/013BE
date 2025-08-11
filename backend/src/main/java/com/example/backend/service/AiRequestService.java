package com.example.backend.service;
import com.example.backend.dto.AiResponseDto;
import com.example.backend.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.File;

@Service
public class AiRequestService {

    // application.properties 파일 AI 서버 주소
    @Value("${ai.server.url}")
    private String aiServerUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * processVideoDev: ai api 요청 횟수 제한에 따라 우선은 더미 데이터로 실행
     * -> 실제 ai 분석 요청 동작할 땐 processVideo 메소드 사용하기
      */
    public AiResponseDto processVideoDev(File videoFile) {
        AiResponseDto aiResponseDto = new AiResponseDto();

        String name = "박진영";
        String age = "83세"; // // '60대 후반'과 같이 소개하는 경우를 위해 String 으로 설정해둠 (Integer로 나중에 변경해도 됨)
        String hobbies = "[\n\"종이 접기\",\n\"과학\",\n\"사랑\"\n]"; // AI 서버가 hobbies를 JSON 문자열로 줌

        aiResponseDto.setName(name);
        aiResponseDto.setAge(age);
        aiResponseDto.setHobbies(hobbies);

        return aiResponseDto;
    }

    public AiResponseDto processVideo(File videoFile) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("video", new FileSystemResource(videoFile));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // AI 서버로 POST 요청을 보내고, 응답을 AiResponseDto로 받음
        return restTemplate.postForObject(aiServerUrl, requestEntity, AiResponseDto.class);
    }
}