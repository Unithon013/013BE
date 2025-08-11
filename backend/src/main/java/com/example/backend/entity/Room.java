package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "rooms")
public class Room extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 마지막 메시지는 자주 업데이트: 성능을 위해) 별도 컬럼으로 관리
    private String lastMessage;

    // Room은 여러 참가자를 가짐 (두 명)
    @Builder.Default // 빌더 사용 시 이 기본값을 사용하도록 어노테이션 추가
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participant> participants = new ArrayList<>();

    // Room은 여러 메시지를 가짐
    @Builder.Default
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    public void addParticipant(Participant participant) {
        participants.add(participant);
        participant.setRoom(this);
    }

    public void addMessage(Message message) {
        messages.add(message);
        message.setRoom(this);
    }

}