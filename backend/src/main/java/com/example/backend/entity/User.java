package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column // default 세팅
    private String name;

    private String age; // '60대 후반'과 같이 소개하는 경우를 위해 String 으로 설정해둠 (Integer로 나중에 변경해도 됨)

    @Enumerated(EnumType.STRING)
    @Column // 실제 어떻게 입력 처리할 지 상의해보고 nullable false 세팅
    private Gender gender;
    public enum Gender{M, F}

    @Column(columnDefinition = "TEXT")
    private String hobbies;  // JSON 형식 텍스트 저장

    @Column(name="video_url", nullable = false)
    private String videoUrl;

    private String location; // 관악, 동작, 상도 와 같은 짧은 지역 표시를 위한 필드 // -> prepersist || 위치 입력받을 때 처리 필요
    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private Integer point = 0;

    @Column(name = "profile_url")
    private String profileUrl;

//    // User는 여러 채팅방에 참여 가능
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<Participant> participants = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Status status; // AI 분석 상태를 저장할 필드

    public enum Status {
        PROCESSING, // 처리 중
        COMPLETE,   // 처리 완료
        FAILED      // 처리 실패
    }
}