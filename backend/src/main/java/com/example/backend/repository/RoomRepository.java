package com.example.backend.repository;

import com.example.backend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    /**
     * 두 명의 사용자가 모두 참여하고 있는 채팅방을 찾는 쿼리
     * @param userId1 사용자 ID 1
     * @param userId2 사용자 ID 2
     * @return 두 사용자가 공유하는 채팅방 (존재하지 않으면 Optional.empty())
     */
    @Query("SELECT r FROM Room r JOIN r.participants p1 JOIN r.participants p2 " +
            "WHERE p1.user.id = :userId1 AND p2.user.id = :userId2")
    Optional<Room> findExistingRoom(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
