package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "recommendations")
public class Recommendation extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 추천 사용자 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 추천된 사용자 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_user_id", nullable = false)
    private User recommendedUser;

    @Column(nullable = false)
    private LocalDate date;
}