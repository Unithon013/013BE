package com.example.backend.repository;

import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 현재 위치를 기반으로 특정 반경(km) 내의 다른 사용자들을 찾는 쿼리
     * @param latitude 현재 사용자 위도
     * @param longitude 현재 사용자 경도
     * @param distance 검색할 반경 (km)
     * @param excludeUserIds 제외할 사용자 ID 목록 (본인, 이미 추천한 사람 등)
     * @return 추천 대상 사용자 목록
     */
    @Query(value = "SELECT * FROM users u WHERE u.id NOT IN :excludeUserIds AND " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(u.latitude)) * " +
            "cos(radians(u.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
            "sin(radians(u.latitude)))) < :distance " +
            "ORDER BY RAND() LIMIT :limit", nativeQuery = true)
    List<User> findUsersNearBy(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("distance") double distance,
            @Param("excludeUserIds") List<Long> excludeUserIds,
            @Param("limit") int limit);

    /** 간단 거리+취미 필터링 쿼리
     * @param latitude 현재 사용자 위도
     * @param longitude 현재 사용자 경도
     * @param hobby
     * @return 추천 대상 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.id <> :userId AND " +
            " (6371 * acos(cos(radians(:lat)) * cos(radians(u.latitude)) * cos(radians(u.longitude) - radians(:lon)) + sin(radians(:lat)) * sin(radians(u.latitude)))) < 50 " +
            " AND u.hobbies LIKE CONCAT('%', :hobby, '%')")
    List<User> findUsersByLocationAndHobbies(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("hobby") String hobby);
    
}
