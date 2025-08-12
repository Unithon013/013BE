package com.example.backend.repository;

import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 현재 위치 기반으로 특정 반경 내 '다른 성별'의 사용자들을 찾는 쿼리
     * @param latitude 현재 사용자 위도
     * @param longitude 현재 사용자 경도
     * @param distance 검색할 반경 (km)
     * @param excludeUserIds 제외할 사용자 ID 목록 (본인, 이미 추천한 사람 등)
     * @param currentUserGender 현재 사용자의 성별 (반대 성별을 찾기 위함)
     * @return 추천 대상 사용자 목록
     */
    @Query(value = "SELECT * FROM users u WHERE u.id NOT IN :excludeUserIds AND u.gender <> :currentUserGender AND " + // [수정] 이성만 필터링
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(u.latitude)) * " +
            "cos(radians(u.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
            "sin(radians(u.latitude)))) < :distance " +
            "LIMIT :limit", nativeQuery = true) // [수정] ORDER BY RAND() 제거
    List<User> findUsersNearBy(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("distance") double distance,
            @Param("excludeUserIds") List<Long> excludeUserIds,
            @Param("currentUserGender") String currentUserGender, // [추가] 현재 사용자 성별 파라미터
            @Param("limit") int limit);

    @Query(value = "SELECT * FROM users u " +
            "WHERE u.id <> :userId " +
            "AND u.gender <> :currentUserGender " +
            "AND (6371 * acos(" +
            "   cos(radians(:latitude)) * cos(radians(u.latitude)) * " +
            "   cos(radians(u.longitude) - radians(:longitude)) + " +
            "   sin(radians(:latitude)) * sin(radians(u.latitude))" +
            ")) < 50 " +
            "AND u.hobbies LIKE CONCAT('%', :hobby, '%')",
            nativeQuery = true)
    List<User> findUsersByLocationAndHobbies(
            @Param("userId") Long userId,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("hobby") String hobby);

}
