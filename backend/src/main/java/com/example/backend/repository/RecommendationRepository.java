package com.example.backend.repository;

import com.example.backend.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    /**
     * 오늘 사용자가 추천받은 인원
     * @param userId 사용자
     * @param date 오늘 날짜
     * @return 추천 인원 리스트
     */
    @Query("SELECT r.recommendedUser.id FROM Recommendation r " +
            "WHERE r.user.id = :userId AND r.date = :date")
    List<Long> findRecommendedUserIdsByUserIdAndDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date);

    /**
     * 오늘 사용자가 추천받은 인원 수 세기
     * @param userId 사용자
     * @param date 오늘 날짜
     * @return 추천 인원 수
     */
    @Query("SELECT COUNT(r) FROM Recommendation r " +
            "WHERE r.user.id = :userId AND r.date = :date")
    long countByUserIdAndDate(
            @Param("userId") Long userId,
            @Param("date") LocalDate date);
}