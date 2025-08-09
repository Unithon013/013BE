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

    @Column(columnDefinition = "TEXT")
    private String hobbies;  // JSON 형식 텍스트 저장

    @Column(name="video_url", nullable = false)
    private String videoUrl;

    private String location;
    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private Integer point = 0;

//    // User는 여러 채팅방에 참여 가능
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<Participant> participants = new ArrayList<>();

}