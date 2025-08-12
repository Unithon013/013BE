package com.example.backend.repository;

import com.example.backend.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByUserId(Long userId);

    /**
     * 특정 채팅방에서 상대방(해당 사용자가 아닌 다른 참가자)를 찾는 쿼리
     * @param roomId 특정 채팅방
     * @param userId 특정 사용자
     * @return 상대방 
     */
    Optional<Participant> findByRoomIdAndUserIdNot(Long roomId, Long userId);

    /**
     * 특정 사용자와 현재 대화 중인 모든 상대방의 ID 목록을 조회하는 쿼리
     * @param userId 현재 사용자 ID
     * @return 대화 중인 모든 상대방 User ID 리스트
     */
    @Query("SELECT p.user.id FROM Participant p WHERE p.room.id IN " +
            "(SELECT p2.room.id FROM Participant p2 WHERE p2.user.id = :userId) AND p.user.id != :userId")
    List<Long> findChattingPartnerIdsByUserId(@Param("userId") Long userId);
}