package com.example.backend.service;
import com.example.backend.dto.AiResponseDto;
import com.example.backend.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.util.Map;

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
        String gender = "M";

        aiResponseDto.setName(name);
        aiResponseDto.setAge(age);
        aiResponseDto.setHobbies(hobbies);
        aiResponseDto.setGender(gender);

        return aiResponseDto;
    }

    /**
     * 실제 ai 요청 처리하는 함수
     * @param videoFile 분석 요청할 영상 파일
     * @return
     */
    public AiResponseDto processVideo(File videoFile) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("video", new FileSystemResource(videoFile));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // AI 서버로 POST 요청을 보내고, 응답을 AiResponseDto로 받음
        return restTemplate.postForObject(aiServerUrl, requestEntity, AiResponseDto.class);
    }


    /**
     * 백그라운드 비동기 처리: 1. AI 서버에 분석을 '요청'하고 'task_id'를 받아오는 메서드
     * @param videoFile 분석 요청할 영상 파일
     * @return task_id
     */
    public String requestVideoProcessing(File videoFile) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("video", new FileSystemResource(videoFile));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // AI 서버의 /process-video로 요청을 보내고, 응답에서 task_id를 추출
        ResponseEntity<Map> response = restTemplate.postForEntity(aiServerUrl + "/process-video", requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.ACCEPTED && response.getBody() != null) {
            return (String) response.getBody().get("task_id");
        }
        throw new RuntimeException("AI 서버 작업 요청 실패");
    }

    /**
     * 백그라운드 비동기 처리: 2. 'task_id'로 최종 분석 결과를 '조회'하는 메서드
     * @param taskId 1번에서 AI 서버에 분석 요청 후 받아온 'task_id' 값
     * @return 처리 완료 시 AiResponseDto
     */
    public AiResponseDto getProcessingResult(String taskId) {
        // AI 서버의 /tasks/{taskId}로 GET 요청
        String url = aiServerUrl + "/tasks/" + taskId;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response != null && "completed".equals(response.get("status"))) {
            // ObjectMapper를 사용하여 Map을 DTO로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.convertValue(response.get("result"), AiResponseDto.class);
        }
        // 아직 처리 중이거나 실패한 경우 null 반환
        return null;
    }
}