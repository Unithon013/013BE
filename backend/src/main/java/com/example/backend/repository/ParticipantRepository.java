package com.example.backend.repository;

import com.example.backend.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
