package com.example.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class Coord2AddressResponseDto {
    private List<Document> documents;

    @Data
    public static class Document {
        private Address address;
    }

    @Data
    public static class Address {
        private String region_1depth_name; // 시 or 도
        private String region_2depth_name; // 시 or 군 or 구
    }
}
