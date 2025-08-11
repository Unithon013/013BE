package com.example.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.backend.dto.Coord2AddressResponseDto;

@Service
@RequiredArgsConstructor
public class LocationService {

    @Value("${kakao.rest-api-key}")
    private String kakaoRestApiKey;

    @Value("${kakao.local.coord2address-url}")
    private String coord2AddressUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getCityAndDistrict(double lat, double lng) {
        String url = UriComponentsBuilder.fromHttpUrl(coord2AddressUrl)
                .queryParam("x", lng) // x=경도, y=위도
                .queryParam("y", lat)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Coord2AddressResponseDto> resp =
                restTemplate.exchange(url, HttpMethod.GET, entity, Coord2AddressResponseDto.class);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null
                || resp.getBody().getDocuments() == null || resp.getBody().getDocuments().isEmpty()) {
            return null;
        }

        var address = resp.getBody().getDocuments().get(0).getAddress();
        if (address != null) {
            String city = address.getRegion_1depth_name();    // 서울특별시
            String district = address.getRegion_2depth_name(); // 동작구
            if (city != null && district != null) {
                return city + " " + district;
            }
        }
        return null;
    }
}
